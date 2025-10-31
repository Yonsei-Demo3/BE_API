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
import java.util.List;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-ms}") long accessValidityMs,
            @Value("${app.jwt.refresh-token-validity-ms}") long refreshValidityMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    public String createAccessToken(String userId, String role) {
        return buildToken(userId, role, accessValidityMs);
    }

    public String createRefreshToken(String userId) {
        // 리프레시는 최소 정보만 (role 불필요)
        return buildToken(userId, null, refreshValidityMs);
    }

    private String buildToken(String subject, String role, long validity) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) builder.claim("role", role);
        return builder.compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String userId = body.getSubject();
        String role = body.get("role", String.class); // access 토큰에는 존재
        var auth = (role != null)
                ? new SimpleGrantedAuthority("ROLE_" + role)
                : null;
        return new UsernamePasswordAuthenticationToken(userId, token, auth == null ? List.of() : List.of(auth));
    }

    public long getAccessValidityMs()  { return accessValidityMs; }
    public long getRefreshValidityMs() { return refreshValidityMs; }
}
