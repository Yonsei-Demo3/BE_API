package com.backend.scrap.dto;

import java.time.LocalDateTime;

public record ScrappedMessageSummaryDTO(
    Long messageId,
    String messageContent,
    Long questionId,
    String questionTitle,
    Long contentId,
    String contentTitle,
    Long scrapCount,
    LocalDateTime latestScrappedAt
) {}
