package com.backend.content.service;

import com.backend.categoryContent.CategoryContent;
import com.backend.categoryContent.CategoryContentRepository;
import com.backend.content.domain.Content;
import com.backend.content.dto.ContentSearchResponseDTO;
import com.backend.content.dto.ContentSearchDTO;
import com.backend.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentSearchService {

    private final ContentRepository contentRepository;
    private final CategoryContentRepository categoryContentRepository;

    /**
     * 콘텐츠 이름 부분 검색 + 카테고리 정보까지 묶어서 반환
     */
    public ContentSearchResponseDTO searchContents(String keyword, Pageable pageable) {

        // 1) 콘텐츠 이름 검색
        Page<Content> page = contentRepository.findByNameContainingIgnoreCase(keyword, pageable);
        List<Content> contents = page.getContent();
    
        if (contents.isEmpty()) {
            return new ContentSearchResponseDTO(keyword, 0, List.of());
        }
    
        // 2) ID 목록
        List<Long> contentIds = contents.stream()
                .map(Content::getId)
                .toList();
    
        // 3) CategoryContent 한번에 조회
        List<CategoryContent> links =
                categoryContentRepository.findByContentIdIn(contentIds);
    
        // 4) 카테고리 이름 매핑
        Map<Long, List<String>> categoryNamesByContentId = links.stream()
                .collect(Collectors.groupingBy(
                        cc -> cc.getContent().getId(),
                        Collectors.mapping(
                                cc -> cc.getCategory().getName(),
                                Collectors.toList()
                        )
                ));
    
        // 5) Content → ContentSearchDTO 변환
        List<ContentSearchDTO> items = contents.stream()
                .map(content -> {
                    List<String> categories =
                            categoryNamesByContentId.getOrDefault(content.getId(), List.of());
                    String categoryText = String.join(" / ", categories);
    
                    return new ContentSearchDTO(
                            content.getId(),
                            content.getName(),
                            categoryText,
                            content.getImageUrl()
                    );
                })
                .toList();
    
        // 6) 최종 Response 생성
        return new ContentSearchResponseDTO(
                keyword,
                (int) page.getTotalElements(),
                items
        );
    }
}
