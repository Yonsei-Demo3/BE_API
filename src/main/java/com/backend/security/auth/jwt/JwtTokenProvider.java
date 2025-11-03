package com.backend.security.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import com.backend.security.auth.user.CustomUserPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Date;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
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
                // jti 부여
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                // 이 토큰을 누가 발급했는지(우리 서비스에서 발급한 것인가)
                .setIssuer(issuer)
                // 이 토큰을 누가 사용할 수 있는지(어떤 클라이언트를 위한 것인가)
                .setAudience(audience)
                // 토큰 종류 구분(access, refresh)
                .claim("typ", type)
                .claim(CLAIM_TOKEN_TYPE, type)
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) builder.claim("role", role);
        return builder.compact();
    }

    // === 파서 & 클레임 헬퍼 ===
    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(clockSkewSec)
                .build();
    }

    public Claims parseClaims(String token) {
        return parser().parseClaimsJws(token).getBody();
    }

    public Optional<String> tryGetJti(String token) {
        try { return Optional.ofNullable(parseClaims(token).getId()); }
        catch (Exception e) { return Optional.empty(); }
    }

    public Date getExpiresAt(String token) {
        return parseClaims(token).getExpiration();
    }

    public long getExpiresInSeconds(String token) {
        Date exp = getExpiresAt(token);
        return Math.max(0, (exp.getTime() - System.currentTimeMillis()) / 1000);
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // === 검증 ===
    public JwtValidationResult validateAndClassify(String token) {
        return validateAndClassify(token, Set.of(audience));
    }

    public JwtValidationResult validateAndClassify(String token, Set<String> acceptedAudiences) {
        try {
            Jws<Claims> jws = parser().parseClaimsJws(token);
            Claims c = jws.getBody();

            // issuer
            if (!issuer.equals(c.getIssuer())) {
                return JwtValidationResult.INVALID_ISSUER;
            }

            // audience
            if (acceptedAudiences != null && !acceptedAudiences.isEmpty()) {
                if (c.getAudience() == null || !acceptedAudiences.contains(c.getAudience())) {
                    return JwtValidationResult.INVALID_AUDIENCE;
                }
            } else {
                if (!audience.equals(c.getAudience())) {
                    return JwtValidationResult.INVALID_AUDIENCE;
                }
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
        return validateAndClassify(token, Set.of(audience)) == JwtValidationResult.OK;
    }

    public Authentication getAuthentication(String token) {
        Claims body = parser().parseClaimsJws(token).getBody();
        // subject = memberId 로 발급하고 있으니 Long으로 변환
        Long memberId = Long.parseLong(body.getSubject());
        String role = body.get("role", String.class);

        List<GrantedAuthority> auths = (role != null)
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.of();

        // 이메일을 토큰에 안 넣었다면 null 가능. (원하면 access 토큰에 claim("email", m.getEmail()) 추가)
        String email = body.get("email", String.class);

        CustomUserPrincipal principal = CustomUserPrincipal.builder()
                .memberId(memberId)
                .email(email)
                .authorities(auths)
                .build();

        // principal을 CustomUserPrincipal로!
        return new UsernamePasswordAuthenticationToken(principal, token, auths);
    }

    public Duration getAccessValidity() {
        return Duration.ofMillis(accessValidityMs);
    }

    public Duration getRefreshValidity() {
        return Duration.ofMillis(refreshValidityMs);
    }

    public boolean isAccessToken(String token) {
        String typ = parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
        return "access".equals(typ);
    }

    public boolean isRefreshToken(String token) {
        String typ = parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
        return "refresh".equals(typ);
    }
}
