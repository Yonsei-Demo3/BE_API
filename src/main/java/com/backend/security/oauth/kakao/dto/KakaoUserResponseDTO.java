package com.backend.security.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponseDTO(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String email,
            Profile profile
    ) {
        public record Profile(
                String nickname,
                @JsonProperty("profile_image_url") String profileImageUrl
        ){}
    }
}
