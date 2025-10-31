package com.backend.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * 로그인 요청 DTO
 * - 이메일 / 비밀번호를 전달받음
 */
public record LoginRequestDTO(
        @Email @NotBlank String email,
        @NotBlank
        @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                 message="비밀번호는 8~64자, 영문/숫자 포함")
        String password
) {}
