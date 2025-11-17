package com.backend.room.service;

import com.backend.message.domain.Message;
import com.backend.message.dto.MessageResponseDTO;
import com.backend.message.repository.MessageRepository;
import com.backend.question.repository.QuestionRepository;
import com.backend.room.domain.Room;
import com.backend.room.dto.RoomDTO;
import com.backend.room.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final QuestionRepository questionRepository;

    // TODO: Create는 Question에서 만들기. Question이 생겨야 질문이 생기는 게 맞는 거 같음

    public Page<RoomDTO> listRooms(Pageable pageable) {
        Page<Room> chatroomPage = roomRepository.findAll(pageable);

        //TODO: Question에 대한 정보도 불러와서 같이 넘겨보내줘야함. => DTO 수정
        return chatroomPage.map(RoomDTO::from);
    }

    public RoomDTO getRoomById(Long id) {
        //TODO: Question에 대한 정보도 불러와서 같이 넘겨보내줘야함. => DTO 수정
        Room room = roomRepository.findById(id)
                // 2. 만약 엔티티가 없다면, 예외를 발생시킵니다.
                //    (이 예외는 @RestControllerAdvice 같은 곳에서 잡아서
                //     클라이언트에게 404 Not Found 응답을 보냅니다.)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + id));

        return RoomDTO.from(room);
    }

    public Page<MessageResponseDTO> listMessages(Long roomId, Pageable pageable) {
        //TODO: 자기가 소속된 채팅방인지 확인하는 로직추가
        Page<Message> messagePage = messageRepository.findByRoomId(roomId, pageable);
        return messagePage.map(MessageResponseDTO::from);
    }
}
