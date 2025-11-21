package com.backend.friend.dto;

public record FriendUserDTO(
        String userId,
        String nickname,
        String profileImageUrl
) {}
