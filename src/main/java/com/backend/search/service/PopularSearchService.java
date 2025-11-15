package com.backend.search.service;

import com.backend.search.dto.KeywordTrendDTO;
import com.backend.search.dto.PopularKeywordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PopularSearchService {

    private final StringRedisTemplate redisTemplate;

    // 현재 집계용 ZSET
    private static final String CURRENT_ZSET_KEY = "popular:search:current";

    // 스냅샷 ZSET prefix (예: popular:search:snapshot:2025111601)
    private static final String SNAPSHOT_KEY_PREFIX = "popular:search:snapshot:";

    private static final DateTimeFormatter SNAPSHOT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 검색어 카운트 +1
     */
    public void increase(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) return; // 공백/너무 짧은 검색어 등은 무시

        redisTemplate.opsForZSet()
                .incrementScore(CURRENT_ZSET_KEY, keyword, 1.0);
    }

    /**
     * 상위 N개 인기 검색어 (키워드 + 카운트)
     */
    public List<PopularKeywordDTO> getTopKeywordsWithCount(int size) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(CURRENT_ZSET_KEY, 0, size - 1);

        if (tuples == null) return List.of();

        return tuples.stream()
        .map(t -> {
            String value = t.getValue();
            Double score = t.getScore();
            if (value == null || score == null) return null;
            return new PopularKeywordDTO(value, score.longValue());
        })
        .filter(Objects::nonNull)
        .toList();
    }

    /**
     * 순위 변화까지 포함한 인기 검색어 (현재 vs 가장 최근 스냅샷)
     */
    public List<KeywordTrendDTO> getTrending(int size) {
        // 1) 현재 인기 검색어
        List<PopularKeywordDTO> current = getTopKeywordsWithCount(size);

        if (current.isEmpty()) {
            return List.of();
        }

        // 2) 가장 최근 스냅샷 키 찾기
        String latestSnapshotKey = findLatestSnapshotKey();
        if (latestSnapshotKey == null) {
            // 스냅샷이 아직 없다면 전부 NEW 처리
            List<KeywordTrendDTO> result = new ArrayList<>();
            for (int i = 0; i < current.size(); i++) {
                PopularKeywordDTO dto = current.get(i);
                int rank = i + 1;
                result.add(new KeywordTrendDTO(
                        dto.keyword(),
                        dto.count(),
                        rank,
                        null,        // 이전 순위 없음
                        "NEW"        // NEW로 표시
                ));
            }
            return result;
        }

        // 3) 스냅샷에서 상위 N개 가져오기
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(latestSnapshotKey, 0, size - 1);

        Map<String, Integer> prevRankMap = new HashMap<>();
        if (tuples != null) {
            int idx = 1;
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t.getValue() == null) continue;
                prevRankMap.put(t.getValue(), idx++);
            }
        }

        // 4) 현재 랭킹과 이전 랭킹 비교해서 Trend 생성
        List<KeywordTrendDTO> result = new ArrayList<>();
        for (int i = 0; i < current.size(); i++) {
            PopularKeywordDTO dto = current.get(i);
            String keyword = dto.keyword();
            int nowRank = i + 1;

            Integer prevRank = prevRankMap.get(keyword);

            String movement; // UP / DOWN / SAME / NEW
            if (prevRank == null) {
                movement = "NEW";
            } else if (prevRank == nowRank) {
                movement = "SAME";
            } else if (prevRank > nowRank) {
                movement = "UP";      // 순위 상승
            } else {
                movement = "DOWN";    // 순위 하락
            }

            result.add(new KeywordTrendDTO(
                    keyword,
                    dto.count(),
                    nowRank,
                    prevRank,
                    movement
            ));
        }

        return result;
    }

    /**
     * 현재 인기검색어 상위 N개를 스냅샷 ZSET에 복사
     *  - 스케줄러에서 주기적으로 호출
     */
    public void snapshotTopKeywords(int size) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(CURRENT_ZSET_KEY, 0, size - 1);

        if (tuples == null || tuples.isEmpty()) return;

        String snapshotKey = SNAPSHOT_KEY_PREFIX
                + LocalDateTime.now().format(SNAPSHOT_FORMATTER);

        // 기존 스냅샷 키가 있다면 삭제 후 다시 저장 (idempotent하게)
        redisTemplate.delete(snapshotKey);

        for (ZSetOperations.TypedTuple<String> t : tuples) {
                
            String value = t.getValue();
            Double score = t.getScore();

            if (value == null || score == null) continue;
            redisTemplate.opsForZSet()
                    .add(snapshotKey, value, score);
        }

        // 스냅샷 TTL (예: 7일) — Duration 안 쓰고 TimeUnit으로 처리
        redisTemplate.expire(snapshotKey, 7L * 24L * 60L * 60L, TimeUnit.SECONDS);
    }

    // 가장 최근 스냅샷 키 찾기 (키 개수가 많지 않다는 가정)
    private String findLatestSnapshotKey() {
        Set<String> keys = redisTemplate.keys(SNAPSHOT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return null;

        // yyyyMMddHH 형식이라 문자열 정렬의 max가 가장 최신
        return Collections.max(keys);
    }

    private String normalize(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim();
        if (k.length() < 2) return null; // 1글자짜리 검색어는 통계에서 제외
        return k.toLowerCase();          // 영문 검색어용 (한글이면 영향 없음)
    }
}
