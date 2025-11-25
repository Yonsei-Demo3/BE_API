package com.backend.friend.dto;

public record FriendRequestCreateRequestDTO(
        Long targetMemberId,
        String message
) {}
