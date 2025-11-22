package com.backend.content.dto;

public record ContentSearchDTO(
    Long contentId,
    String name,              // 콘텐츠명 (서사의 위기)
    String categoryText,
    String thumbnailUrl       // 썸네일 이미지 URL
) {}
