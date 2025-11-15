package com.backend.search.controller;

import com.backend.search.dto.KeywordTrendDTO;
import com.backend.search.dto.PopularKeywordDTO;
import com.backend.search.service.PopularSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search", description = "검색 / 인기 검색어 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class PopularSearchController {

    private final PopularSearchService popularSearchService;

    @Operation(summary = "인기 검색어 TOP N (단순 카운트)")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularKeywordDTO>> getPopularKeywords(
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                popularSearchService.getTopKeywordsWithCount(size)
        );
    }

    @Operation(summary = "인기 검색어 TOP N + 순위 변화")
    @GetMapping("/popular/trending")
    public ResponseEntity<List<KeywordTrendDTO>> getTrendingKeywords(
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                popularSearchService.getTrending(size)
        );
    }
}
