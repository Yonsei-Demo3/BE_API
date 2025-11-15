package com.backend.search.controller;

import com.backend.search.service.RecentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class RecentSearchController {

    private final RecentSearchService recentSearchService;

    // 🔍 최근 검색어 조회
    @GetMapping("/recent")
    public List<String> getRecent(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestParam(defaultValue = "5") int size
    ) {
        // 비로그인은 최근 검색어 없음
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        return recentSearchService.getRecentKeywords(userId, size);
    }

    // 🗑 특정 검색어 삭제
    @DeleteMapping("/recent")
    public void deleteOne(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestParam String keyword
    ) {
        if (userId == null || userId.isBlank()) return;
        recentSearchService.deleteKeyword(userId, keyword);
    }

    // 🧹 전체 삭제
    @DeleteMapping("/recent/all")
    public void clearAll(
            @AuthenticationPrincipal(expression = "userId") String userId
    ) {
        if (userId == null || userId.isBlank()) return;
        recentSearchService.clearAll(userId);
    }
}
