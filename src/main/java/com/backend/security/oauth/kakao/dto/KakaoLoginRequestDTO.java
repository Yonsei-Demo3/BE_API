package com.backend.security.oauth.kakao.dto;

import jakarta.validation.constraints.NotBlank;

// 클라이언트로부터 받는 인가코드 DTO
public record KakaoLoginRequestDTO(
        @NotBlank String code
) {}
