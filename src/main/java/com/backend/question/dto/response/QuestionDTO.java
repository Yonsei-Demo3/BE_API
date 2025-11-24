package com.backend.question.dto.response;

import com.backend.question.domain.Question;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record QuestionDTO(
        Long questionId,
        Long roomId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static QuestionDTO from(Question question) {
        return new QuestionDTO(question.getId(), question.getRoom().getId(), question.getCreatedAt(), question.getUpdatedAt());
    }
}
