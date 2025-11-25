package com.backend.friend.dto;

public record FriendSummaryDTO(
        Long memberId,
        String nickname,
        String email,
        String profileImage
) {}
