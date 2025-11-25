package com.backend.question.dto.request;

import com.backend.question.domain.QuestionStartMode;

import java.util.List;

//TODO: Validation 적용
public record CreateFirstQuestionRequestDTO(
        String title,
        String description,
        int maxParticipants,
        QuestionStartMode startMode,
        Long contentId,
        List<String> tags
) {
}
