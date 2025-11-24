package com.backend.roomMember.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomMemberStatus {
    JOINED("모집 참여"),
    READY("수락함"),
    DROPPED("취소");

    private final String description;

}
