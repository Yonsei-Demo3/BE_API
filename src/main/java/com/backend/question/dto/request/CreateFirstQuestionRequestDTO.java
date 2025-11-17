package com.backend.question.dto.request;

import java.util.List;

//TODO: Validation 적용
public record CreateFirstQuestionRequestDTO(
        String title,
        String description,
        int maxParticipants,
        Long contentId,
        List<String> tags
) {
}
