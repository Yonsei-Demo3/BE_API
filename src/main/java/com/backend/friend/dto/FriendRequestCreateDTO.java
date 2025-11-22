package com.backend.friend.dto;

public record FriendRequestCreateDTO(
        String targetUserId,   // 친구 신청 받을 유저의 userId
        String message         // 친구 신청 메세지 (null/빈 문자열 허용)
) {}