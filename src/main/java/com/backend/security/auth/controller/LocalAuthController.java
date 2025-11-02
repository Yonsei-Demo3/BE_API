package com.backend.security.auth.controller;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.security.auth.domain.RefreshToken;
import com.backend.security.auth.dto.LoginRequestDTO;
import com.backend.security.auth.dto.RefreshRequestDTO;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.jwt.JwtTokenProvider;
import com.backend.security.auth.jwt.JwtValidationResult;
import com.backend.security.auth.repository.RefreshTokenRepository;
import com.backend.security.auth.service.AuthTokenService;
import com.backend.security.auth.blacklist.TokenBlacklistService;
import com.backend.security.util.HashUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;

@Tag(name = "Auth", description = "인증 API (JWT + Refresh)")
@RestController
@RequestMapping("/api/auth")
public class LocalAuthController {

    private final MemberRepository memberRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthTokenService authTokenService;
    private final TokenBlacklistService blacklist;

    public LocalAuthController(MemberRepository memberRepo,
                          RefreshTokenRepository refreshRepo,
                          PasswordEncoder encoder,
                          JwtTokenProvider tokenProvider,
                          AuthTokenService authTokenService,
                          TokenBlacklistService blacklist) {
        this.memberRepo = memberRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.tokenProvider = tokenProvider;
        this.authTokenService = authTokenService;
        this.blacklist = blacklist;
    }

    @Operation(summary = "로그인 → Access/Refresh 동시 발급")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        Member m = memberRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!encoder.matches(req.password(), m.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 토큰 발급 + 저장(로테이션) 공통 로직
        return ResponseEntity.ok(authTokenService.issueTokensFor(m));
    }

    @Operation(summary = "재발급 → Access 교체, Refresh 로테이션(보안 권장)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO req) {
        final String oldRefresh = req.refreshToken();
        String oldRefreshHash = HashUtil.sha256Base64(oldRefresh);

        // 1) JWT 자체 유효성 + 발급자/대상자 + 형식 검사
        JwtValidationResult vr = tokenProvider.validateAndClassify(oldRefresh, Set.of("web")); // audience 정책에 맞게
        if (vr != JwtValidationResult.OK) {
            return unauthorized("invalid_token", "refresh token validation failed: " + vr);
        }
        if (!tokenProvider.isRefreshToken(oldRefresh)) { // typ == "refresh" 검사 (provider에 추가해 둔 메서드)
            return unauthorized("invalid_token", "token is not a refresh token");
        }

        // 2) 서버 저장소에 실제 존재하는 refresh 인지 확인 (도난/폐기 토큰 차단)
        RefreshToken saved = refreshRepo.findByTokenHash(oldRefreshHash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (saved.isExpired()) {
            refreshRepo.deleteByUserId(saved.getUserId()); // 만료된 기록 정리
            return unauthorized("invalid_token", "refresh token expired");
        }

        // 3) 주인(Member) 확인 후 새 토큰 세트 발급(+DB 로테이션)
        Member owner = memberRepo.findByUserId(saved.getUserId())
                .orElseThrow(() -> new IllegalStateException("해당 사용자가 존재하지 않습니다."));

        TokenResponseDTO newSet = authTokenService.issueTokensFor(owner); // 기존 레코드가 로테이션됨
        return ResponseEntity.ok(newSet);
    }

    @Operation(summary = "로그아웃 → Access 블랙리스트 등록 + Refresh 제거")
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    
        // 0) Authorization 헤더 존재 & 형식 확인
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\", error_description=\"missing or malformed Authorization header\"")
                    .build();
        }
    
        final String accessToken = authorizationHeader.substring(7).trim();
    
        // 1) 토큰 유효성 (+audience) & 타입(access) 확인
        JwtValidationResult vr = tokenProvider.validateAndClassify(accessToken, Set.of("web"));
        if (vr != JwtValidationResult.OK || !tokenProvider.isAccessToken(accessToken)) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\", error_description=\"access token validation failed: " + vr + "\"")
                    .build();
        }
    
        // 2) jti 추출
        Optional<String> jtiOpt = tokenProvider.tryGetJti(accessToken);
        if (jtiOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\", error_description=\"missing jti\"")
                    .build();
        }
        String jti = jtiOpt.get();
    
        // 3) 남은 TTL 계산 (음수/0 방지)
        Instant now = Instant.now();
        Instant exp = tokenProvider.getExpiresAt(accessToken).toInstant();
        Duration ttl = Duration.between(now, exp);
        if (!ttl.isNegative() && !ttl.isZero()) {
            // 정상 TTL
        } else {
            ttl = Duration.ofSeconds(1);
        }
    
        // 4) Redis 블랙리스트 등록
        blacklist.blacklist(jti, ttl);
    
        // 5) 토큰 subject(=userId)로 refresh 기록 제거
        String userId = tokenProvider.getSubject(accessToken);
        refreshRepo.deleteByUserId(userId);
    
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<TokenResponseDTO> unauthorized(String code, String desc) {
        return ResponseEntity.status(401)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"" + code + "\", error_description=\"" + desc + "\"")
                .body(null);
    }
}
