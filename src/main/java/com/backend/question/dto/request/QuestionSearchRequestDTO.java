package com.backend.question.dto.request;

import java.util.List;

public record QuestionSearchRequestDTO(
        String keyword,
        List<CategorySearchPair> categories,
        List<String> tags
) {
    public record CategorySearchPair(
            String main,
            String sub
    ) {}
}
