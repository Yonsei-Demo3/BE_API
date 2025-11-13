package com.backend.content.dto;

import com.backend.content.domain.Content;

public record ContentResponseDTO(Long contentId, String name) {

    public static ContentResponseDTO from(Content content) {
        return new ContentResponseDTO(content.getId(), content.getName());
    }
}
