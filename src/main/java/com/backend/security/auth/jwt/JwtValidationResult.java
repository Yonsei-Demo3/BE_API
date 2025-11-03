package com.backend.security.auth.jwt;

// JWT 인증 과정이 왜 실패했는지 이유를 구체화
public enum JwtValidationResult {
    OK,
    EXPIRED,
    MALFORMED,
    INVALID_SIGNATURE,
    INVALID_ISSUER,
    INVALID_AUDIENCE,
    BLACKLISTED
}
