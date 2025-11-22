package com.backend.friend.controller;

import com.backend.friend.dto.FollowCountDTO;
import com.backend.friend.dto.FriendUserDTO;
import com.backend.friend.dto.FollowResponseDTO;
import com.backend.friend.service.FriendService;
import com.backend.security.auth.user.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend", description = "팔로우/팔로워(친구) API")
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "팔로우")
    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowResponseDTO> follow(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable String targetUserId
    ) {
        FollowResponseDTO dto = friendService.follow(me.getUserId(), targetUserId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "언팔로우")
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<FollowResponseDTO> unfollow(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable String targetUserId
    ) {
        FollowResponseDTO dto = friendService.unfollow(me.getUserId(), targetUserId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "팔로우 여부 조회 (me → target)")
    @GetMapping("/{targetUserId}/following")
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @PathVariable String targetUserId
    ) {
        boolean following = friendService.isFollowing(me.getUserId(), targetUserId);
        return ResponseEntity.ok(following);
    }

    @Operation(summary = "팔로워/팔로잉 카운트 조회")
    @GetMapping("/{userId}/counts")
    public ResponseEntity<FollowCountDTO> getFollowCounts(
            @PathVariable String userId
    ) {
        FollowCountDTO dto = friendService.getFollowCounts(userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "팔로잉 리스트 조회")
    @GetMapping("/{userId}/followings")
    public ResponseEntity<List<FriendUserDTO>> getFollowings(
            @PathVariable String userId
    ) {
        List<FriendUserDTO> list = friendService.getFollowings(userId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "팔로워 리스트 조회")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FriendUserDTO>> getFollowers(
            @PathVariable String userId
    ) {
        List<FriendUserDTO> list = friendService.getFollowers(userId);
        return ResponseEntity.ok(list);
    }
}
