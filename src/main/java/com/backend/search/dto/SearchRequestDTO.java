package com.backend.search.dto;

import java.util.List;

public record SearchRequestDTO(
    String keyword,
    List<Long> categoryIds
) {}
