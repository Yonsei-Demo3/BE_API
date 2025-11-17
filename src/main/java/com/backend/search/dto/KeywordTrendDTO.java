package com.backend.search.dto;

/**
 * 인기 검색어 + 순위 변화 정보 DTO
 */
public record KeywordTrendDTO(
        String keyword,
        long count,
        int rank,             // 현재 순위 (1-based)
        Integer previousRank, // 이전 순위 (없으면 null)
        String movement       // "UP" / "DOWN" / "SAME" / "NEW"
) {}
