package com.backend.scrap.dto;

import java.time.LocalDateTime;

/**
 * 내 스크랩 목록 조회용 DTO
 *
 * message 내용이나 room 정보가 필요하면
 * Message 엔티티에서 가져다가 추가하면 됨.
 */
public record ScrapMessageDTO(
        Long scrapId,
        Long messageId,
        Long roomId,
        String content,
        LocalDateTime scrappedAt
) {}
