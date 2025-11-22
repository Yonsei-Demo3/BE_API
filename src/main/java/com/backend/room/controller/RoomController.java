package com.backend.room.controller;

import com.backend.message.dto.MessageResponseDTO;
import com.backend.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}/messages")
public class RoomController {
    private final RoomService roomService;

    /*
    //TODO: 시큐리티 로직 추가
    @GetMapping
    public ResponseEntity<Page<MessageResponseDTO>> findMessagesByRoomId(@PathVariable Long roomId, @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 30)Pageable pageable) {

        Page<MessageResponseDTO> messagePage = roomService.listMessages(roomId, pageable);
        return ResponseEntity.ok(messagePage);
    }

     */
}
