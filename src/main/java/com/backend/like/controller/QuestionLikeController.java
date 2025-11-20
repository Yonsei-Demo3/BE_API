package com.backend.like.controller;

import com.backend.like.dto.QuestionLikeResponseDTO;
import com.backend.like.service.QuestionLikeService;
import com.backend.security.auth.exception.AuthError;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Question Like", description = "질문 좋아요 API")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionLikeController {

    private final QuestionLikeService questionLikeService;

    @Operation(summary = "질문 좋아요")
    @PostMapping("/{questionId}/like")
    public ResponseEntity<QuestionLikeResponseDTO> like(@PathVariable Long questionId) {
        String userId = getCurrentUserId();

        long count = questionLikeService.likeQuestion(userId, questionId);

        return ResponseEntity.ok(
                new QuestionLikeResponseDTO(questionId, count, true)
        );
    }

    @Operation(summary = "질문 좋아요 취소")
    @DeleteMapping("/{questionId}/like")
    public ResponseEntity<QuestionLikeResponseDTO> unlike(@PathVariable Long questionId) {
        String userId = getCurrentUserId();

        long count = questionLikeService.unlikeQuestion(userId, questionId);

        return ResponseEntity.ok(
                new QuestionLikeResponseDTO(questionId, count, false)
        );
    }

    @Operation(summary = "질문 좋아요 상태 조회")
    @GetMapping("/{questionId}/like")
    public ResponseEntity<QuestionLikeResponseDTO> getStatus(@PathVariable Long questionId) {
        String userId = getCurrentUserId();

        long count = questionLikeService.getLikeCount(questionId);
        boolean liked = questionLikeService.hasLiked(userId, questionId);

        return ResponseEntity.ok(
                new QuestionLikeResponseDTO(questionId, count, liked)
        );
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }
    
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserPrincipal me) {
            return String.valueOf(me.getUserId());   // or me.getUserId() 그대로
        }
    
        throw new AuthError("unauthorized", "유효하지 않은 인증 정보입니다.");
    }
}
