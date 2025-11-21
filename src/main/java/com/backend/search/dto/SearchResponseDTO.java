package com.backend.search.dto;

import java.util.List;

public record SearchResponseDTO (
    List<QuestionSearchItemDTO> questions,
    List<ContentSearchItemDTO> contents,
    List<TagSearchItemDTO> tags
) {}
