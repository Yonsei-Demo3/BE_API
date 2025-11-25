package com.backend.friend.controller;

import com.backend.friend.dto.BlockResponseDTO;
import com.backend.friend.service.FriendService;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Block", description = "친구 차단 API")
@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final FriendService friendService;

    @Operation(summary = "사용자 차단")
    @PostMapping("/{targetMemberId}")
    public ResponseEntity<Void> block(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long targetMemberId
    ) {
        friendService.block(me.getUserId(), targetMemberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "차단 해제")
    @DeleteMapping("/{targetMemberId}")
    public ResponseEntity<Void> unblock(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long targetMemberId
    ) {
        friendService.unblock(me.getUserId(), targetMemberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 차단 목록 조회")
    @GetMapping
    public ResponseEntity<List<BlockResponseDTO>> getMyBlocks(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ResponseEntity.ok(
                friendService.getMyBlocks(me.getUserId())
        );
    }
}
