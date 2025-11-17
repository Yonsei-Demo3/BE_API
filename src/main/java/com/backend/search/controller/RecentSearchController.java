package com.backend.search.controller;

import com.backend.search.service.RecentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.constraints.Max;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Tag(name = "Search - Recent", description = "최근 검색어 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class RecentSearchController {

    private final RecentSearchService recentSearchService;

    // 🔍 최근 검색어 조회
    @Operation(summary = "최근 검색어 조회", description = "로그인한 사용자의 최근 검색어를 최신순으로 조회합니다.")
    @GetMapping("/recent")
    public List<String> getRecent(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestParam(defaultValue = "5")
            @Max(5) int size
    ) {
        // 비로그인은 최근 검색어 없음
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        return recentSearchService.getRecentKeywords(userId, size);
    }

    // 🗑 특정 검색어 삭제
    @Operation(summary = "최근 검색어 삭제", description = "특정 검색어를 최근 검색어 목록에서 삭제합니다.")
    @DeleteMapping("/recent")
    public ResponseEntity<Void> deleteOne(  
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestParam String keyword
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        } 
        recentSearchService.deleteKeyword(userId, keyword);
        return ResponseEntity.noContent().build(); // 204
    }

    // 🧹 전체 삭제
    @Operation(summary = "최근 검색어 전체 삭제", description = "해당 사용자의 최근 검색어 목록을 모두 삭제합니다.")
    @DeleteMapping("/recent/all")
    public ResponseEntity<Void> clearAll(
            @AuthenticationPrincipal(expression = "userId") String userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
    
        recentSearchService.clearAll(userId);
        return ResponseEntity.noContent().build(); // 204
    }
}
