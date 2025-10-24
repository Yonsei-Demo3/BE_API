package com.backend.security.auth.dto;

/**
 * 로그인 및 토큰 재발급 응답 DTO
 * - AccessToken / RefreshToken / 만료시간 정보 포함
 */
public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {}
