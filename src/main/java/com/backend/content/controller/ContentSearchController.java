package com.backend.content.controller;

import com.backend.content.dto.ContentSearchResponseDTO;
import com.backend.content.service.ContentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Content Search", description = "질문 생성용 콘텐츠 검색 API")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentSearchController {

    private final ContentSearchService contentSearchService;

    @Operation(summary = "콘텐츠 검색", description = "콘텐츠에 대해 썸네일/카테고리/콘텐츠명을 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<ContentSearchResponseDTO> search(
        @RequestParam String keyword,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        ContentSearchResponseDTO result = contentSearchService.searchContents(keyword, pageable);
        return ResponseEntity.ok(result);
    }
}
