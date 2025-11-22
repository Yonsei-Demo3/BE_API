package com.backend.message;

import com.backend.message.dto.MessageResponseDTO;
import com.backend.security.auth.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{roomId}")
    public ResponseEntity<List<MessageResponseDTO>> findMessagesByRoomId( @AuthenticationPrincipal CustomUserPrincipal me ,@PathVariable Long roomId) {
        String userId = me.getUserId();

        List<MessageResponseDTO> responseDTOS = messageService.getMessagesByRoomId(userId, roomId);
        return ResponseEntity.ok(responseDTOS);
    }



}
