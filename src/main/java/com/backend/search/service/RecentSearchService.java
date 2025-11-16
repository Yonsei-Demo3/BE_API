package com.backend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "recent:search:";  // recent:search:{userId}
    private static final int MAX_HISTORY = 5;                  // 유저당 최대 최근 검색어 개수

    /**
     * 최근 검색어 추가 (중복 시 앞으로 당기기)
     */
    public void addKeyword(String userId, String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) return;
        
        String key = key(userId);
        if (key == null || key.isBlank()) return;

        // 1) 기존에 있으면 제거
        redisTemplate.opsForList().remove(key, 0, keyword);

        // 2) 가장 앞에 추가 (최신이 index 0)
        redisTemplate.opsForList().leftPush(key, keyword);

        // 3) 길이 제한
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY - 1);

        // 4) 너무 오래된 유저 히스토리는 자ㅌ동 만료 (예: 7일)
        redisTemplate.expire(key, Objects.requireNonNull(Duration.ofDays(7)));
    }

    /**
     * 최근 검색어 조회 (최신순)
     */
    public List<String> getRecentKeywords(String userId, int size) {
        String key = key(userId);
        if (key == null || key.isBlank()) {
            return Collections.emptyList();
        }

        List<String> range = redisTemplate.opsForList()
                .range(key, 0, size - 1);

        return (range != null) ? range : Collections.emptyList();
    }

    /**
     * 특정 검색어만 삭제 (최근 검색어 리스트에서 제거)
     */
    public void deleteKeyword(String userId, String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) return;

        String key = key(userId);
        if (key == null || key.isBlank()) return;

        redisTemplate.opsForList().remove(key, 0, keyword);
    }

    /**
     * 전체 최근 검색어 삭제
     */
    public void clearAll(String userId) {
        String key = key(userId);
        if (key == null || key.isBlank()) return;
        redisTemplate.delete(key);
    }

    // ----------------- 내부 유틸 -----------------

    private String key(String userId) {
                if (userId == null || userId.isBlank()) {
            return null;
        }
        return KEY_PREFIX + userId;
    }

    private String normalize(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.length() < 1) return null;   // 필요하면 2 이상으로 올려도 됨
        return k;
    }
}
