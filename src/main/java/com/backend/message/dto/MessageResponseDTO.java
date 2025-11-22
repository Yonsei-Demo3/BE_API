package com.backend.message.dto;

import com.backend.message.domain.Message;

//TODO: DTO에 담는 내용 상세화
public record MessageResponseDTO(
        Long messageId,
        String content,
        Long senderId,
        String senderNickname,
        boolean isMine
) {
    public static MessageResponseDTO from(Message message, Long currentUserId) {
        boolean isMine = message.getMember().getId().equals(currentUserId);
        return new MessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getMember().getId(),
                message.getMember().getNickname(),
                isMine);

    }
}
