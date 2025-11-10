package com.backend.content.dto;

public record ContentCreateRequestDTO(
        String name, String creator, String description, String imageUrl, String link, String mainCategory, String subCategory
) {
}
