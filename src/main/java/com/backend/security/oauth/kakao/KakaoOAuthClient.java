package com.backend.security.oauth.kakao;

import com.backend.security.oauth.kakao.dto.KakaoTokenResponseDTO;
import com.backend.security.oauth.kakao.dto.KakaoUserResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class KakaoOAuthClient {

    private final WebClient webClient = WebClient.builder().build();
    private final KakaoOAuthProperties props;

    public KakaoOAuthClient(KakaoOAuthProperties props) {
        this.props = props;
    }

    // 1) 인가코드 → 카카오 액세스 토큰
    public KakaoTokenResponseDTO exchangeCodeForToken(String code, String redirectUri) {
        String body = "grant_type=authorization_code" +
                "&client_id=" + enc(props.getClientId()) +
                (isBlank(props.getClientSecret()) ? "" : "&client_secret=" + enc(props.getClientSecret())) +
                "&redirect_uri=" + enc(redirectUri) +
                "&code=" + enc(code);

        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();
    }

    // 2) 카카오 액세스 토큰 → 사용자 정보
    public KakaoUserResponseDTO fetchUser(String kakaoAccessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponseDTO.class)
                .block();
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
