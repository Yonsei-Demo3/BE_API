package com.backend.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 * - 이메일 / 비밀번호를 전달받음
 */
public record LoginRequestDTO(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
