package com.backend.security.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityMs;
    private final long refreshValidityMs;
    private final String issuer;
    private final String audience;
    // 서버간 시각 차이로 인한 오류 방지
    private final long clockSkewSec;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-ms}") long accessValidityMs,
            @Value("${app.jwt.refresh-token-validity-ms}") long refreshValidityMs,
            @Value("${app.jwt.issuer:my-api}") String issuer,
            @Value("${app.jwt.audience:web}") String audience,
            @Value("${app.jwt.clock-skew-sec:60}") long clockSkewSec
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSec = clockSkewSec;
    }

    // === 토큰 생성 ===
    public String createAccessToken(String userId, String role) {
        return buildToken(userId, role, accessValidityMs, "access");
    }

    public String createRefreshToken(String userId) {
        return buildToken(userId, null, refreshValidityMs, "refresh");
    }

    private String buildToken(String subject, String role, long validity, String type) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                // 이 토큰을 누가 발급했는지(우리 서비스에서 발급한 것인가)
                .setIssuer(issuer)
                // 이 토큰을 누가 사용할 수 있는지(어떤 클라이언트를 위한 것인가)
                .setAudience(audience)
                // 토큰 종류 구분(access, refresh)
                .claim("typ", type)
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) builder.claim("role", role);
        return builder.compact();
    }

    // === 검증 ===
    public JwtValidationResult validateAndWhy(String token, Set<String> acceptedAudiences) {
        try {
            Jws<Claims> jws = parser().parseClaimsJws(token);
            Claims c = jws.getBody();

            // issuer 검증
            if (!issuer.equals(c.getIssuer()))
                return JwtValidationResult.INVALID_ISSUER;

            // audience 검증
            if (acceptedAudiences != null && !acceptedAudiences.isEmpty()) {
                if (c.getAudience() == null || !acceptedAudiences.contains(c.getAudience()))
                    return JwtValidationResult.INVALID_AUDIENCE;
            } else {
                if (!audience.equals(c.getAudience()))
                    return JwtValidationResult.INVALID_AUDIENCE;
            }

            return JwtValidationResult.OK;
        } catch (ExpiredJwtException e) { // 유효기간이 지났는지
            return JwtValidationResult.EXPIRED;
        } catch (io.jsonwebtoken.security.SecurityException e) { // 토큰이 서버의 비밀키로 서명된 것인지
            return JwtValidationResult.INVALID_SIGNATURE;
        } catch (JwtException | IllegalArgumentException e) { // JWT 구조가 올바른지
            return JwtValidationResult.MALFORMED;
        }
    }

    public boolean validate(String token) {
        return validateAndWhy(token, Set.of(audience)) == JwtValidationResult.OK;
    }

    public Authentication getAuthentication(String token) {
        Claims body = parser().parseClaimsJws(token).getBody();
        String userId = body.getSubject();
        String role = body.get("role", String.class);
        var auth = (role != null)
                ? new SimpleGrantedAuthority("ROLE_" + role)
                : null;
        return new UsernamePasswordAuthenticationToken(userId, token, auth == null ? List.of() : List.of(auth));
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(clockSkewSec)
                .build();
    }

    public Duration getAccessValidity() {
        return Duration.ofMillis(accessValidityMs);
    }

    public Duration getRefreshValidity() {
        return Duration.ofMillis(refreshValidityMs);
    }
}
