package com.backend.security.oauth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth.kakao")
public class KakaoOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
