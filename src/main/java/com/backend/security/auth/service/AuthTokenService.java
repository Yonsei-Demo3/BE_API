package com.backend.security.auth.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.security.auth.domain.RefreshToken;
import com.backend.security.util.HashUtil;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.jwt.JwtTokenProvider;
import com.backend.security.auth.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthTokenService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshRepo;
    
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
    @Transactional
    public TokenResponseDTO issueTokensFor(Member member) {

        final String userId = member.getUserId();

        // 1️⃣ Access / Refresh 토큰 생성
        String access  = tokenProvider.createAccessToken(userId, member.getRole().name());
        String refresh = tokenProvider.createRefreshToken(userId);

        // 2️⃣ Refresh 토큰 해시 계산
        String refreshHash = HashUtil.sha256Base64(refresh);

        System.out.println("[LOGIN] userId=" + userId
                + " refreshHash=" + refreshHash.substring(0, 12) + "...");

        /// 3️⃣ 만료 시각 계산
        Instant refreshExp = Instant.now().plus(tokenProvider.getRefreshValidity());

        // 4️⃣ DB에 저장 (있으면 rotate, 없으면 새로 생성)
        refreshRepo.findByUserId(userId)
        .ifPresentOrElse(
                rt -> {
                    System.out.println("[LOGIN] rotate existing RT row, id=" + rt.getId());
                    rt.rotate(refreshHash, refreshExp);
                    refreshRepo.save(rt);
                },
                () -> {
                    System.out.println("[LOGIN] insert new RT row");
                    refreshRepo.save(new RefreshToken(userId, refreshHash, refreshExp));
                }
            );

    // 5️⃣ 응답 DTO 리턴 (클라엔트엔 원문 refresh 전달)
    return new TokenResponseDTO(
        access,
        refresh,
        "Bearer",
        tokenProvider.getAccessValidity().toSeconds(),
        tokenProvider.getRefreshValidity().toSeconds()
    );
    }
}