package com.backend.search.controller;

import com.backend.search.dto.SearchRequestDTO;
import com.backend.search.dto.SearchResponseDTO;
import com.backend.search.service.PopularSearchService;
import com.backend.search.service.RecentSearchService;
import com.backend.search.service.SearchService;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search - Unified", description = "통합 검색 API (질문 + 콘텐츠 + 태그)")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final RecentSearchService recentSearchService;
    private final PopularSearchService popularSearchService;

    @Operation(summary = "통합 검색", description = "질문, 콘텐츠, 태그를 한 번에 검색합니다.")
    @PostMapping("/unified")
    public ResponseEntity<SearchResponseDTO> unifiedSearch(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody SearchRequestDTO request,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String keyword = request.keyword();

        // 1) 최근 검색어 + 인기 검색어 반영
        if (keyword != null && !keyword.isBlank()) {
            if (me != null) {
                recentSearchService.addKeyword(me.getUserId(), keyword);
            }
            popularSearchService.increase(keyword);
        }

        // 2) 실제 검색
        SearchResponseDTO result = searchService.unifiedSearch(keyword, pageable);
        return ResponseEntity.ok(result);
    }
}
