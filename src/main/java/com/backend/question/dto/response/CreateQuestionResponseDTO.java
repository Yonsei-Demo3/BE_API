package com.backend.question.dto.response;

import com.backend.question.domain.Question;
import com.backend.room.domain.Room;

public record CreateQuestionResponseDTO(
        Long questionId,
        Long roomId
) {
    public static CreateQuestionResponseDTO from(Question question) {
        return new CreateQuestionResponseDTO(question.getId(), question.getRoom().getId());
    }
}
