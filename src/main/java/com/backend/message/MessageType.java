package com.backend.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트 메시지"),
    IMAGE("이미지");

    private final String description;
}
