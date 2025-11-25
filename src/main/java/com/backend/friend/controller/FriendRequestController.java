package com.backend.friend.controller;

import com.backend.friend.dto.FriendRequestCreateRequestDTO;
import com.backend.friend.dto.FriendRequestResponseDTO;
import com.backend.friend.service.FriendService;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FriendRequest", description = "친구 요청 API")
@RestController
@RequestMapping("/api/v1/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기")
    @PostMapping
    public ResponseEntity<FriendRequestResponseDTO> sendRequest(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody FriendRequestCreateRequestDTO dto
    ) {
        FriendRequestResponseDTO res =
                friendService.sendFriendRequest(me.getUserId(), dto);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "받은 친구 요청 목록(PENDING)")
    @GetMapping("/incoming")
    public ResponseEntity<List<FriendRequestResponseDTO>> getIncoming(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ResponseEntity.ok(
                friendService.getIncomingPendingRequests(me.getUserId())
        );
    }

    @Operation(summary = "보낸 친구 요청 목록(PENDING)")
    @GetMapping("/outgoing")
    public ResponseEntity<List<FriendRequestResponseDTO>> getOutgoing(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ResponseEntity.ok(
                friendService.getOutgoingPendingRequests(me.getUserId())
        );
    }

    @Operation(summary = "친구 요청 수락")
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> accept(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long requestId
    ) {
        friendService.acceptRequest(me.getUserId(), requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "친구 요청 거절")
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long requestId
    ) {
        friendService.rejectRequest(me.getUserId(), requestId);
        return ResponseEntity.ok().build();
    }
}
