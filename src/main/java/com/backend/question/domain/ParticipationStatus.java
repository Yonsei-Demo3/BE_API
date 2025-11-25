package com.backend.question.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationStatus {

    NONE("참여 신청 안 함"),
    WAITING("대기 중"),
    JOINED("참여 중");

    private final String description;
}
