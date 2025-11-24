package com.backend.content.dto;

public record ContentWithCategoriesResponseDTO(
    Long contentId,
    String name,
    String creator,
    String description,
    String imageUrl,
    String link,
    java.util.List<ContentCategoryDTO> categories
) {}
