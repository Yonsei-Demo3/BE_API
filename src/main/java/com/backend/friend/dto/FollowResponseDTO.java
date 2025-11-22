package com.backend.friend.dto;

public record FollowResponseDTO(
        String targetUserId,
        boolean following
) {}
