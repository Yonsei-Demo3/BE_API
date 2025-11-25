package com.backend.scrap.controller;

import com.backend.scrap.dto.MessageScrapResponseDTO;
import com.backend.scrap.dto.ScrapMessageDTO;
import com.backend.scrap.service.MessageScrapService;
import com.backend.security.auth.exception.AuthError;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import com.backend.scrap.dto.ScrappedMessageSummaryDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

@Tag(name = "Message Scrap", description = "채팅 메시지 스크랩 API")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageScrapController {

    private final MessageScrapService messageScrapService;

    @Operation(summary = "메시지 스크랩")
    @PostMapping("/{messageId}/scrap")
    public ResponseEntity<MessageScrapResponseDTO> scrap(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long messageId
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        String userId = me.getUserId();
        MessageScrapResponseDTO dto = messageScrapService.scrapFromRoom(userId, messageId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "메시지 스크랩 취소")
    @DeleteMapping("/{messageId}/scrap")
    public ResponseEntity<MessageScrapResponseDTO> unscrap(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long messageId
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        String userId = me.getUserId();
        MessageScrapResponseDTO dto = messageScrapService.unscrap(userId, messageId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "내 스크랩 메시지 목록 조회")
    @GetMapping("/scrap/me")
    public ResponseEntity<List<ScrapMessageDTO>> getMyScraps(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        String userId = me.getUserId();
        List<ScrapMessageDTO> list = messageScrapService.getScraps(userId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "특정 사용자의 스크랩 메시지 목록 조회")
    @GetMapping("/scrap/{userId}")
    public ResponseEntity<List<ScrapMessageDTO>> getUserScraps(
            @PathVariable String userId
    ) {
        List<ScrapMessageDTO> list = messageScrapService.getScraps(userId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "스크랩 많이 된 메시지 TOP N")
    @GetMapping("/scrap/popular")
    public ResponseEntity<List<ScrappedMessageSummaryDTO>> getHighlights(
            @RequestParam(name = "size", defaultValue = "10")
            @Min(1)
            @Max(50)
            int size
    ) {
        return ResponseEntity.ok(
            messageScrapService.getTopScrappedMessages(size)
        );
    }
}
