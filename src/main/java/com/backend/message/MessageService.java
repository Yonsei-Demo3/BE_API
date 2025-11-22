package com.backend.message;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.message.domain.Message;
import com.backend.message.dto.MessageResponseDTO;
import com.backend.message.repository.MessageRepository;
import com.backend.roomMember.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true )
public class MessageService {
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final RoomMemberRepository roomMemberRepository;

    //TODO: 내가 이방 멤버 인가 확인
    public List<MessageResponseDTO> getMessagesByRoomId(String userId, Long roomId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Long memberId = member.getId();


        List<Message> messages = messageRepository.findByRoomId(roomId);

        return messages.stream()
                .map(msg -> MessageResponseDTO.from(msg, memberId))
                .toList();
    }
}
