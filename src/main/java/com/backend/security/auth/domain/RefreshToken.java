package com.backend.security.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_member", columnList = "memberId", unique = true),
        @Index(name = "idx_refresh_token", columnList = "tokenHash", unique = true)
})

@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable=false, length = 500)
    private String tokenHash;

    @Column(nullable=false)
    private Instant expiresAt;

    protected RefreshToken() {}

    public RefreshToken(Long memberId, String tokenHash, Instant expiresAt) {
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void rotate(String newHash, Instant newExp) {
        this.tokenHash = newHash;
        this.expiresAt = newExp;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}