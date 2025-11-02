package com.backend.room.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    PREPARING("시작 전"),
    OPEN("진행 중"),
    CLOSED("종료 됨");

    private final String description;

}
