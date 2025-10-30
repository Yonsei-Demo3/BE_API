package com.backend.security.auth.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.security.auth.domain.RefreshToken;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.jwt.JwtTokenProvider;
import com.backend.security.auth.repository.RefreshTokenRepository;

@Service
public class AuthTokenService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshRepo;
    private static final long MS_TO_SECONDS = 1000L;

    public AuthTokenService(JwtTokenProvider tokenProvider,
                            RefreshTokenRepository refreshRepo) {
        this.tokenProvider = tokenProvider;
        this.refreshRepo = refreshRepo;
    }

    /**
     * 주어진 회원에 대해 access/refresh 토큰을 생성하고
     * refresh 토큰을 DB에 upsert/rotate 한 다음
     * 클라이언트에게 내려줄 TokenResponseDTO로 변환해 돌려준다.
     */
    public TokenResponseDTO issueTokensFor(Member member) {

        // 1) 새 토큰 발급
        String access  = tokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
        String refresh = tokenProvider.createRefreshToken(member.getEmail());

        // 2) 만료시각 계산
        Instant refreshExp = Instant.now().plusMillis(tokenProvider.getRefreshValidityMs());

        // 3) refresh 토큰 DB 반영 (있으면 rotate, 없으면 insert)
        refreshRepo.findByEmail(member.getEmail())
                .ifPresentOrElse(
                        rt -> {
                            rt.rotate(refresh, refreshExp);
                            refreshRepo.save(rt);
                        },
                        () -> refreshRepo.save(new RefreshToken(member.getEmail(), refresh, refreshExp))
                );

        // 4) 응답 DTO로 만들어 반환
        return new TokenResponseDTO(
                access,
                refresh,
                "Bearer",
                tokenProvider.getAccessValidityMs() / MS_TO_SECONDS,
                tokenProvider.getRefreshValidityMs() / MS_TO_SECONDS
        );
    }
}