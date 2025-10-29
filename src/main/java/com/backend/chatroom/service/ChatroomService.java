package com.backend.chatroom.service;

import com.backend.chatroom.domain.Chatroom;
import com.backend.chatroom.dto.ChatroomDTO;
import com.backend.chatroom.repository.ChatroomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ChatroomService {
    private final ChatroomRepository chatroomRepository;

    @Transactional
    public ChatroomDTO createChatroom(ChatroomDTO dto) {
        Chatroom chatroom = Chatroom.of(dto.name(), dto.description());
        chatroomRepository.save(chatroom);
        return ChatroomDTO.from(chatroom);
    }

    public Page<ChatroomDTO> listChatrooms(Pageable pageable) {
        Page<Chatroom> chatroomPage = chatroomRepository.findAll(pageable);


        return chatroomPage.map(ChatroomDTO::from);
    }

    public ChatroomDTO getChatroomById(Long id) {

        Chatroom chatroom = chatroomRepository.findById(id)
                // 2. 만약 엔티티가 없다면, 예외를 발생시킵니다.
                //    (이 예외는 @RestControllerAdvice 같은 곳에서 잡아서
                //     클라이언트에게 404 Not Found 응답을 보냅니다.)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + id));

        return ChatroomDTO.from(chatroomRepository.findById(id).orElse(null));
    }
}
