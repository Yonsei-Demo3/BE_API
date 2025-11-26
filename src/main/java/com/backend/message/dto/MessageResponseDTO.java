package com.backend.message.dto;

import com.backend.message.domain.Message;
import com.backend.message.domain.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

//TODO: DTO에 담는 내용 상세화
public record MessageResponseDTO(
        Long messageId,
        String content,
        Long senderId,
        String senderNickname,
        boolean isMine,
        MessageType type,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt

) {
    public static MessageResponseDTO from(Message message, Long currentUserId) {
        boolean isMine = message.getMember().getId().equals(currentUserId);
        return new MessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getMember().getId(),
                message.getMember().getNickname(),
                isMine,
                message.getType(),
                message.getCreatedAt()
        );

    }
}
