package com.backend.security.auth.dto;

/**
 * 서비스 내부에서만 쓸 발급 결과 DTO(Access + Refresh)
 */
public record TokenIssueResultDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {}
