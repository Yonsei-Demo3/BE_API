package com.backend.friend.dto;

import com.backend.friend.domain.FriendRequestStatus;

public record FriendRequestResponseDTO(
        Long requestId,
        Long fromMemberId,
        Long toMemberId,
        String message,
        FriendRequestStatus status
) {}
