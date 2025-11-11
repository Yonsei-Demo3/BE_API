package com.backend.content.dto;

import com.backend.category.domain.Category;
import com.backend.content.domain.Content;

public record ContentResponseDTO(Long contentId, String name, String mainCategory, String subCategory) {

    //TODO: getParent null 발생가능성
    public static ContentResponseDTO from(Content content, Category subCategory) {
        return new ContentResponseDTO(content.getId(), content.getName(), subCategory.getParent().getName(), subCategory.getName());
    }
}
