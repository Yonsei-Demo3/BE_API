package com.backend.friend.domain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendRequestStatus {
    PENDING("친구 요청 대기중"),
    ACCEPTED("친구 요청 수락"),
    REJECTED("친구 요청 거절");

    private final String description;
}
