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
                .requestMatchers("/healthz", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll() // 회원가입 공개
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/page", "/callback", "/api/v1/auth/oauth/kakao/callback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/questions/*/like").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/questions/*/like").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/questions/*/like").authenticated()
                .requestMatchers("/api/v1/questions/search").permitAll()
                .requestMatchers("/api/v1/search").permitAll()
                .requestMatchers("/api/v1/contents/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")             // 관리자 전용
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
