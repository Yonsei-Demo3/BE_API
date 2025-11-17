package com.backend.security.auth.exception;

import lombok.Getter;

@Getter
public class AuthError extends RuntimeException {
    private final String code;
    private final String desc;

    public AuthError(String code, String desc) {
        super(desc);
        this.code = code;
        this.desc = desc;
    }

    public static AuthError invalidRefreshToken() {
        return new AuthError("INVALID_REFRESH_TOKEN", "Refresh token is missing or empty.");
    }
}
