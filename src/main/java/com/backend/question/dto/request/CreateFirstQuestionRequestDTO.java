package com.backend.question.dto.request;

//TODO: Validation 적용
public record CreateFirstQuestionRequestDTO(
        String title,
        String description,
        int maxParticipants,
        Long contentId
) {
}
