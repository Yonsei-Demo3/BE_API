package com.backend.member.domain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("일반회원"),
    ADMIN("관리자");

    private final String description;
}
