package com.backend.content.service;

import com.backend.category.domain.Category;
import com.backend.category.repository.CategoryRepository;
import com.backend.categoryContent.CategoryContent;
import com.backend.categoryContent.CategoryContentRepository;
import com.backend.content.domain.Content;
import com.backend.content.dto.ContentCreateRequestDTO;
import com.backend.content.dto.ContentResponseDTO;
import com.backend.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {
    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryContentRepository categoryContentRepository;

    @Transactional
    public ContentResponseDTO createContent(ContentCreateRequestDTO dto) {
        Content content = Content.of(dto.name(), dto.creator(), dto.description(), dto.imageUrl(), dto.link());
        Category mainCategory = categoryRepository.findByName(dto.mainCategory())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Category subCategory = categoryRepository.findByNameAndParent(dto.subCategory(), mainCategory)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Content savedContent= contentRepository.save(content);

        CategoryContent categoryContent = CategoryContent.of(subCategory, content);
        categoryContentRepository.save(categoryContent);


        return ContentResponseDTO.from(savedContent, subCategory);
    }
}
