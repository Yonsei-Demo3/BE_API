package com.backend.question.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionStatus {
    PREPARING("시작 전"),
    OPEN("진행 중"),
    CLOSED("종료 됨");

    private final String description;

}
