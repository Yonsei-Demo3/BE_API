package com.backend.security.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_email_unique", columnList = "email", unique = true),
        @Index(name = "idx_refresh_token", columnList = "token")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 255)
    private String email;

    @Column(nullable=false, length = 500)
    private String token;

    @Column(nullable=false)
    private Instant expiresAt;

    protected RefreshToken() {}

    public RefreshToken(String email, String token, Instant expiresAt) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }

    public void rotate(String newToken, Instant newExpiry) {
        this.token = newToken;
        this.expiresAt = newExpiry;
    }

    public boolean isExpired() { return Instant.now().isAfter(this.expiresAt); }
}