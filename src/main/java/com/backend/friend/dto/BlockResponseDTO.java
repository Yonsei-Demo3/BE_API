package com.backend.friend.dto;

public record BlockResponseDTO(
        Long blockedMemberId,
        String nickname,
        String email
) {}
