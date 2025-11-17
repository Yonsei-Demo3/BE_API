package com.backend.search.dto;

/**
 * 단순 인기 검색어 + 카운트 응답용 DTO
 */
public record PopularKeywordDTO(
        String keyword,
        long count
) {}
