package com.backend.content.dto;

import java.util.List;

public record ContentSearchResponseDTO(
        String keyword,
        int totalCount,
        List<ContentSearchDTO> items
) {}
