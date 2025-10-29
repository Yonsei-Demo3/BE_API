package com.backend.chatroom.dto;

import com.backend.chatroom.domain.Chatroom;

public record ChatroomDTO(Long id,
                          String name,
                          String description) {
    public static ChatroomDTO from(Chatroom chatroom) {
        return new ChatroomDTO(chatroom.getId(), chatroom.getName(), chatroom.getDescription());
    }
}
