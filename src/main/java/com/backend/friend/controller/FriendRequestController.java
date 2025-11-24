package com.backend.friend.controller;

import com.backend.friend.dto.FriendRequestCreateDTO;
import com.backend.friend.dto.FriendRequestDTO;
import com.backend.friend.service.FriendRequestService;
import com.backend.security.auth.exception.AuthError;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend Request", description = "친구 신청/수락/거절 API")
@RestController
@RequestMapping("/api/v1/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    // ---- 친구 신청 보내기 ----
    @Operation(summary = "친구 요청 보내기")
    @PostMapping
    public ResponseEntity<FriendRequestDTO> sendRequest(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody FriendRequestCreateDTO request
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        FriendRequestDTO dto = friendRequestService.sendRequest(me.getUserId(), request);
        return ResponseEntity.ok(dto);
    }

    // ---- 내가 받은 요청 목록 (PENDING) ----
    @Operation(summary = "받은 친구 요청 목록 (대기중)")
    @GetMapping("/incoming")
    public ResponseEntity<List<FriendRequestDTO>> incoming(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        List<FriendRequestDTO> list = friendRequestService.getIncomingRequests(me.getUserId());
        return ResponseEntity.ok(list);
    }

    // ---- 내가 보낸 요청 목록 ----
    @Operation(summary = "보낸 친구 요청 목록")
    @GetMapping("/outgoing")
    public ResponseEntity<List<FriendRequestDTO>> outgoing(
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        List<FriendRequestDTO> list = friendRequestService.getOutgoingRequests(me.getUserId());
        return ResponseEntity.ok(list);
    }

    // ---- 친구 요청 수락 ----
    @Operation(summary = "친구 요청 수락")
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<FriendRequestDTO> accept(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long requestId
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        FriendRequestDTO dto = friendRequestService.accept(me.getUserId(), requestId);
        return ResponseEntity.ok(dto);
    }

    // ---- 친구 요청 거절 ----
    @Operation(summary = "친구 요청 거절")
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<FriendRequestDTO> reject(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable Long requestId
    ) {
        if (me == null) {
            throw new AuthError("unauthorized", "로그인이 필요합니다.");
        }

        FriendRequestDTO dto = friendRequestService.reject(me.getUserId(), requestId);
        return ResponseEntity.ok(dto);
    }
}
