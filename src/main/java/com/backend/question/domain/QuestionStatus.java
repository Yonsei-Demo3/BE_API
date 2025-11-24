package com.backend.question.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionStatus {
    RECRUITING("모집 중"),
    READY_CHECK("준비 확인 중"),
    ACTIVE("대화 진행 중"),
    FINISHED("대화 종료 됨"),
    CANCELED("대화 취소 됨");

    private final String description;

}
