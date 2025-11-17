package com.backend.question.dto.response;

import com.backend.question.domain.Question;

public record QuestionDTO(
        Long questionId,
        Long roomId
) {
    public static QuestionDTO from(Question question) {
        return new QuestionDTO(question.getId(), question.getRoom().getId());
    }
}
