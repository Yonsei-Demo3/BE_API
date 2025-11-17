package com.backend.security.auth.jwt;

import com.backend.security.auth.blacklist.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklist;

    // 필요 시 허용 audience를 외부에서 주입하고 싶으면 생성자 인자에 Set<String> 추가해서 사용
    private final Set<String> acceptedAudiences;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   TokenBlacklistService blacklist) {
        this(tokenProvider, blacklist, null);
    }

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   TokenBlacklistService blacklist,
                                   Set<String> acceptedAudiences) {
        this.tokenProvider = tokenProvider;
        this.blacklist = blacklist;
        this.acceptedAudiences = acceptedAudiences;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        System.out.println(">>> [JWT FILTER] " + req.getMethod() + " " + req.getRequestURI());

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();

            // 0) 블랙리스트(JTI) 선확인 (jti 파싱 실패 시 아래 검증에서 걸림)
            try {
                String jti = tokenProvider.tryGetJti(token).orElse(null);
                if (jti != null && blacklist.isBlacklisted(jti)) {
                    req.setAttribute("jwt.error", "blacklisted");
                    // 인증 세팅 없이 통과 -> 이후 인가 단계에서 401/403
                    chain.doFilter(req, res);
                    return;
                }
            } catch (Exception ignore) {
                // 계속 진행해서 표준 검증 로직으로 사유 설정
            }

            // 1) 상세 검증 (서명, 만료, iss/aud 등)
            JwtValidationResult result = (acceptedAudiences == null)
                    ? tokenProvider.validateAndClassify(token)
                    : tokenProvider.validateAndClassify(token, acceptedAudiences);
            if (result == JwtValidationResult.OK) {

                // 2) refresh 토큰을 Authorization에 들고 오는 오남용 방지
                if (!tokenProvider.isAccessToken(token)) {
                    req.setAttribute("jwt.error", "refresh-token-not-allowed-in-authorization");
                    chain.doFilter(req, res);
                    return;
                }

                // 3) 인증 컨텍스트 세팅
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // 실패 사유를 리퀘스트에 남겨서 ErrorHandlingFilter 또는 EntryPoint에서 메시지 제어 가능
                req.setAttribute("jwt.error", result.name().toLowerCase());
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // 공개/예외 경로는 필터 스킵
        String p = request.getRequestURI();
        return p.startsWith("/healthz")
            || p.startsWith("/swagger-ui")
            || p.startsWith("/v3/api-docs")
            || p.startsWith("/api/v1/auth/login")
            || p.startsWith("/api/v1/auth/refresh")
            || p.startsWith("/error");
    }
}