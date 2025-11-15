package com.backend.security.auth.service;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.security.auth.blacklist.TokenBlacklistService;
import com.backend.security.auth.domain.RefreshToken;
import com.backend.security.auth.dto.LoginRequestDTO;
import com.backend.security.auth.dto.RefreshRequestDTO;
import com.backend.security.auth.dto.TokenIssueResultDTO;
import com.backend.security.auth.exception.AuthError;
import com.backend.security.auth.jwt.JwtTokenProvider;
import com.backend.security.auth.jwt.JwtValidationResult;
import com.backend.security.auth.repository.RefreshTokenRepository;
import com.backend.security.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthTokenService authTokenService;   // 토큰 발급/로테이션 공통부
    private final TokenBlacklistService blacklist;

    /** 로그인: 이메일/비밀번호 검증 → 토큰세트 발급 */
    @Transactional
    public TokenIssueResultDTO login(LoginRequestDTO req) {
        Member m = memberRepo.findByEmail(req.email())
                .orElseThrow(() -> new AuthError("invalid_grant", "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!encoder.matches(req.password(), m.getPassword())) {
            throw new AuthError("invalid_grant", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return authTokenService.issueTokensFor(m);
    }

    /** 재발급: refresh 검증 → DB 해시 매치 → 로테이션 발급 */
    @Transactional
    public TokenIssueResultDTO refresh(RefreshRequestDTO req) {
        final String oldRefresh = req.refreshToken();
        final String oldRefreshHash = HashUtil.sha256Base64(oldRefresh);

        JwtValidationResult vr = tokenProvider.validateAndClassify(oldRefresh, Set.of("web"));
        if (vr != JwtValidationResult.OK) {
            throw new AuthError("invalid_token", "refresh token validation failed: " + vr);
        }
        if (!tokenProvider.isRefreshToken(oldRefresh)) {
            throw new AuthError("invalid_token", "token is not a refresh token");
        }

        RefreshToken saved = refreshRepo.findByTokenHash(oldRefreshHash)
                .orElseThrow(() -> new AuthError("invalid_token", "유효하지 않은 리프레시 토큰입니다."));

        if (saved.isExpired()) {
            refreshRepo.deleteByUserId(saved.getUserId());
            throw new AuthError("invalid_token", "refresh token expired");
        }

        Member owner = memberRepo.findByUserId(saved.getUserId())
                .orElseThrow(() -> new AuthError("invalid_request", "해당 사용자가 존재하지 않습니다."));

        return authTokenService.issueTokensFor(owner);
    }

    /** 로그아웃: Access 블랙리스트 등록 + 해당 userId 의 refresh 삭제 */
    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthError("invalid_token", "missing or malformed Authorization header");
        }
        final String accessToken = authorizationHeader.substring(7).trim();

        JwtValidationResult vr = tokenProvider.validateAndClassify(accessToken, Set.of("web"));
        if (vr != JwtValidationResult.OK || !tokenProvider.isAccessToken(accessToken)) {
            throw new AuthError("invalid_token", "access token validation failed: " + vr);
        }

        Optional<String> jtiOpt = tokenProvider.tryGetJti(accessToken);
        if (jtiOpt.isEmpty()) {
            throw new AuthError("invalid_token", "missing jti");
        }
        String jti = jtiOpt.get();

        Instant now = Instant.now();
        Instant exp = tokenProvider.getExpiresAt(accessToken).toInstant();
        Duration ttl = Duration.between(now, exp);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofSeconds(1);
        }

        blacklist.blacklist(jti, ttl);

        // subject는 userId (문자열)로 발급 중
        String userId = tokenProvider.getSubject(accessToken);
        refreshRepo.deleteByUserId(userId);
    }
}
