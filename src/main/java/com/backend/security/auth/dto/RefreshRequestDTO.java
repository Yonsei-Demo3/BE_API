package com.backend.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 재발급 요청 DTO
 * - RefreshToken만 전달받음
 */
public record RefreshRequestDTO(
        @NotBlank String refreshToken
) {}