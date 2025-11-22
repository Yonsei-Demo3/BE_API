package com.backend.scrap.dto;

import java.time.LocalDateTime;

/**
 * 메시지 스크랩 토글/조회 결과 DTO
 *
 * @param messageId  스크랩 대상 메시지 ID
 * @param scrapped   현재 유저 기준으로 스크랩 여부
 * @param scrappedAt 스크랩 시각(스크랩 상태가 false인 경우 null)
 */
public record MessageScrapResponseDTO(
        Long messageId,
        boolean scrapped,
        String source,        // "MY_ROOM" | "EXTERNAL"
        LocalDateTime scrappedAt
) {}
