package com.backend.friend.controller;

import com.backend.friend.dto.FriendSummaryDTO;
import com.backend.friend.service.FriendService;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend", description = "친구 관리 API")
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "내 친구 목록 조회")
    @GetMapping("/me")
    public ResponseEntity<List<FriendSummaryDTO>> getMyFriends(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ResponseEntity.ok(
                friendService.getMyFriends(me.getUserId())
        );
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendMemberId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long friendMemberId
    ) {
        friendService.removeFriend(me.getUserId(), friendMemberId);
        return ResponseEntity.ok().build();
    }
}
