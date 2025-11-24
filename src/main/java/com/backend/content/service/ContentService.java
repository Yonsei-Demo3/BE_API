package com.backend.content.service;

import com.backend.category.domain.Category;
import com.backend.category.repository.CategoryRepository;
import com.backend.categoryContent.CategoryContent;
import com.backend.categoryContent.CategoryContentRepository;
import com.backend.content.domain.Content;
import com.backend.content.dto.ContentCreateRequestDTO;
import com.backend.content.dto.ContentResponseDTO;
import com.backend.content.dto.ContentWithCategoriesResponseDTO;
import com.backend.content.dto.ContentCategoryDTO;
import com.backend.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<ContentWithCategoriesResponseDTO> getAllContentsWithCategories() {

        // 1) 모든 콘텐츠 조회
        List<Content> contents = contentRepository.findAll();
        if (contents.isEmpty()) {
            return List.of();
        }

        // 2) 해당 콘텐츠들에 연결된 CategoryContent 전부 한 번에 조회
        List<CategoryContent> mappings = categoryContentRepository.findAllByContentIn(contents);

        // 3) contentId 기준으로 그룹핑
        Map<Long, List<CategoryContent>> byContentId = mappings.stream()
                .collect(Collectors.groupingBy(cc -> cc.getContent().getId()));

        // 4) DTO 변환
        return contents.stream()
                .map(content -> {
                    List<CategoryContent> myMappings =
                            byContentId.getOrDefault(content.getId(), List.of());

                    List<ContentCategoryDTO> categories = myMappings.stream()
                            .map(cc -> {
                                Category sub = cc.getCategory();
                                Category main = sub.getParent();   // 메인 카테고리 (없을 수도 있음)
                                String mainName = (main != null) ? main.getName() : null;
                                String subName  = sub.getName();
                                return new ContentCategoryDTO(mainName, subName);
                            })
                            .toList();

                    return new ContentWithCategoriesResponseDTO(
                            content.getId(),
                            content.getName(),
                            content.getCreator(),
                            content.getDescription(),
                            content.getImageUrl(),
                            content.getLink(),
                            categories
                    );
                })
                .toList();
    }
}
