package com.backend.security.oauth.kakao.dto;

import jakarta.validation.constraints.NotBlank;

// 모바일/프론트 SDK가 준 카카오 access_token을 직접 받는 경우(대체 루트)
public record KakaoAccessTokenLoginRequestDTO(
        @NotBlank String kakaoAccessToken
) {}
