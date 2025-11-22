package com.backend.friend.dto;

import com.backend.friend.domain.FriendRequestStatus;

import java.time.LocalDateTime;

public record FriendRequestDTO(
        Long requestId,
        String requesterUserId,
        String requesterNickname,
        String receiverUserId,
        String receiverNickname,
        String message,
        FriendRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {}
