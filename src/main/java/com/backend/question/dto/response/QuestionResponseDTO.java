package com.backend.question.dto.response;

import com.backend.question.domain.Question;

public record QuestionResponseDTO(Long questionId, Long roomId) {
    public static QuestionResponseDTO from(Question question) {
        return new QuestionResponseDTO(question.getId(),  question.getRoom().getId());
    }

}
