package com.backend.security.auth.controller;

import com.backend.security.auth.domain.RefreshToken;
import com.backend.security.auth.dto.LoginRequestDTO;
import com.backend.security.auth.dto.RefreshRequestDTO;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.jwt.JwtTokenProvider;
import com.backend.security.auth.repository.RefreshTokenRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Auth", description = "인증 API (JWT + Refresh)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberRepository memberRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(MemberRepository memberRepo,
                          RefreshTokenRepository refreshRepo,
                          PasswordEncoder encoder,
                          JwtTokenProvider tokenProvider) {
        this.memberRepo = memberRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "로그인 → Access/Refresh 동시 발급")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        Member m = memberRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!encoder.matches(req.password(), m.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String access = tokenProvider.createAccessToken(m.getEmail(), m.getRole().name());
        String refresh = tokenProvider.createRefreshToken(m.getEmail());
        // DB에 리프레시 토큰 upsert (로테이션 대비)
        Instant rExp = Instant.now().plusMillis(tokenProvider.getRefreshValidityMs());

        refreshRepo.findByEmail(m.getEmail())
                .ifPresentOrElse(
                        rt -> { rt.rotate(refresh, rExp); refreshRepo.save(rt); },
                        () -> refreshRepo.save(new RefreshToken(m.getEmail(), refresh, rExp))
                );

            return ResponseEntity.ok(new TokenResponseDTO(
                access, refresh, "Bearer",
                tokenProvider.getAccessValidityMs() / 1000,
                tokenProvider.getRefreshValidityMs() / 1000
        ));
    }

    @Operation(summary = "재발급 → Access 교체, Refresh 로테이션(보안 권장)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO req) {
        String oldRefresh = req.refreshToken();

        // 1) 토큰 자체 검증 (서명/만료)
        if (!tokenProvider.validate(oldRefresh)) {
            return ResponseEntity.status(401).build();
        }

        // 2) 저장된 리프레시인지 확인 (탈취 방지)
        RefreshToken saved = refreshRepo.findByToken(oldRefresh)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (saved.isExpired()) {
            refreshRepo.deleteByEmail(saved.getEmail());
            return ResponseEntity.status(401).build();
        }

        // 3) 새 Access + 새 Refresh(로테이션) 발급
        Member m = memberRepo.findByEmail(saved.getEmail())
                .orElseThrow(() -> new IllegalStateException("해당 사용자가 존재하지 않습니다."));

        String newAccess  = tokenProvider.createAccessToken(m.getEmail(), m.getRole().name());
        String newRefresh = tokenProvider.createRefreshToken(m.getEmail());
        Instant newExpiry = Instant.now().plusMillis(tokenProvider.getRefreshValidityMs());

        saved.rotate(newRefresh, newExpiry);
        refreshRepo.save(saved);

        return ResponseEntity.ok(new TokenResponseDTO(
                newAccess, newRefresh, "Bearer",
                tokenProvider.getAccessValidityMs() / 1000,
                tokenProvider.getRefreshValidityMs() / 1000
        ));
    }

    @Operation(summary = "로그아웃 → 서버 저장된 Refresh 제거 (Access는 자연 만료)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String email) {
        refreshRepo.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }
}