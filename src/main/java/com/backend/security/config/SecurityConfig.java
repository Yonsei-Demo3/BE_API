package com.backend.security.config;

import com.backend.security.auth.blacklist.TokenBlacklistService;
import com.backend.security.auth.jwt.JwtAuthenticationFilter;
import com.backend.security.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklist;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 공개 API
                .requestMatchers(
                    "/healthz",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error",
                    "/page",
                    "/callback",
                    "/api/v1/auth/**",
                    "/api/v1/auth/oauth/kakao/callback",
                    "/api/v1/questions/search",
                    "/api/v1/contents/**",
                    "/api/v1/search/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()

                // 질문 좋아요
                .requestMatchers(HttpMethod.GET, "/api/v1/questions/*/like").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/questions/*/like").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/questions/*/like").authenticated()

                // 메시지 스크랩
                .requestMatchers(HttpMethod.POST, "/api/v1/messages/*/scrap").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/messages/*/scrap").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/messages/scrap/me").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/v1/messages/scrap/*").authenticated()
                
                // 친구
                .requestMatchers(HttpMethod.POST,   "/api/v1/friends/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/friends/*").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/v1/friends/*/following").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/v1/friends/*/counts").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/v1/friends/*/followings").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/v1/friends/*/followers").permitAll()

                // 관리자 전용
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 나머지
                .anyRequest().authenticated()
            )
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())
            .logout(Customizer.withDefaults());

        // JWT 필터 등록 (블랙리스트 주입, 필요 시 audience도 전달 가능)
        http.addFilterBefore(
                new JwtAuthenticationFilter(tokenProvider, blacklist),
                UsernamePasswordAuthenticationFilter.class
        );
        return http.build();
    }
    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 도메인들 적기 (예시)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://gifpt-front.vercel.app"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // ⭐ 여기에서 Authorization 헤더를 브라우저에 노출
        config.setExposedHeaders(List.of("Authorization"));

        // 쿠키(RefreshToken) 쓰려면 true
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
