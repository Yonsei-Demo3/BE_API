package com.backend.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScrappedMessageSummaryDTO {

    private Long messageId;
    private String messageContent;

    private Long questionId;
    private String questionTitle;

    private Long contentId;
    private String contentTitle;

    private Long scrapCount;
    private LocalDateTime latestScrappedAt;
}
