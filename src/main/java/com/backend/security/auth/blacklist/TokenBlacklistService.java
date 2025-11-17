package com.backend.security.auth.blacklist;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/** 기본 인터페이스 */
public interface TokenBlacklistService {
    void blacklist(String jti, Duration ttl);
    boolean isBlacklisted(String jti);
}

@Service
class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redis;

    public RedisTokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(String jti) { return "jwt:blacklist:" + jti; }

    @Override
    @SuppressWarnings("null")
    public void blacklist(String jti, Duration ttl) {
        Objects.requireNonNull(jti);
        redis.opsForValue().set(key(jti), "1", ttl);
    }

    @Override
    @SuppressWarnings("null")
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(key(jti)));
    }
}
