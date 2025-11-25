package com.backend.scrap.dto;

public record ScrappedMessageSummaryDTO(
    Long messageId,
    long scrapCount
) {}
