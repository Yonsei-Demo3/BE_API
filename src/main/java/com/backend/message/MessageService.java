package com.backend.message;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.message.domain.Message;
import com.backend.message.dto.MessageResponseDTO;
import com.backend.message.repository.MessageRepository;
import com.backend.scrap.repository.MessageScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final MessageScrapRepository messageScrapRepository;

    public List<MessageResponseDTO> getMessagesByRoomId(String userId, Long roomId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Long memberId = member.getId();


        List<Message> messages = messageRepository.findByRoomId(roomId);

        return messages.stream()
                .map(msg -> {
                    boolean isScrapped = messageScrapRepository.existsByMemberAndMessage(member, msg);
                    boolean isMine = msg.getMember().getId().equals(memberId);
                    return MessageResponseDTO.from(msg, isMine, isScrapped);
                })
                .toList();
    }
}
