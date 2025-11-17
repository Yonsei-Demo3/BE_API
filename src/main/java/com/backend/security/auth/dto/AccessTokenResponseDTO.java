package com.backend.security.auth.dto;

/**
 * 클라이언트에게 내려줄 AccessToken 전용 DTO
 */
public record AccessTokenResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
