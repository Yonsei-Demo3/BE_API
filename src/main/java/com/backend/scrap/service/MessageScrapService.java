package com.backend.scrap.service;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.message.domain.Message;
import com.backend.message.repository.MessageRepository;
import com.backend.scrap.domain.MessageScrap;
import com.backend.scrap.dto.MessageScrapResponseDTO;
import com.backend.scrap.dto.ScrapMessageDTO;
import com.backend.scrap.repository.MessageScrapRepository;
import com.backend.security.auth.exception.AuthError;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.backend.scrap.dto.ScrappedMessageSummaryDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageScrapService {

    private final MessageScrapRepository messageScrapRepository;
    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));
        }

    private Message getMessage(Long messageId) {
    return messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메시지입니다."));
    }

    // 
    // 스크랩
    // 
    public MessageScrapResponseDTO scrapFromRoom(String userId, Long messageId) {
        Member me = getMember(userId);
        Message msg = getMessage(messageId);

        return messageScrapRepository.findByMemberAndMessage(me, msg)
                .map(s -> new MessageScrapResponseDTO(msg.getId(), true, s.getScrappedAt()))
                .orElseGet(() -> {
                    MessageScrap saved = messageScrapRepository.save(MessageScrap.of(me, msg));
                    return new MessageScrapResponseDTO(msg.getId(), true, saved.getScrappedAt());
                });
    }

    /**
     * 스크랩 취소 (없어도 에러 내지 않고 조용히 처리)
     */
    public MessageScrapResponseDTO unscrap(String userId, Long messageId) {
        Member me = getMember(userId);
        Message message = getMessage(messageId);
    
        messageScrapRepository.findByMemberAndMessage(me, message)
                .ifPresent(messageScrapRepository::delete);
    
        return new MessageScrapResponseDTO(message.getId(), false, null);
    }

    /**
     * 스크랩 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ScrapMessageDTO> getScraps(String userId, String order) {
        Sort sort;
        if ("oldest".equalsIgnoreCase(order)) {
            sort = Sort.by(Sort.Direction.ASC, "scrappedAt");   // 오래된 순
        } else if ("popular".equalsIgnoreCase(order)) {
            sort = Sort.by(Sort.Direction.DESC, "scrapCount"); // 인기순 (스크랩 수 내림차순)
        } else {
            sort = Sort.by(Sort.Direction.DESC, "scrappedAt"); // 기본: 최신 순
        }
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, sort);
        return messageScrapRepository.findScrapsByUserId(userId, pageRequest).getContent();
    }

    /**
    * 인기 스크랩 조회
    */
    @Transactional(readOnly = true)
    public List<ScrappedMessageSummaryDTO> getTopScrappedMessages(int size) {
        return messageScrapRepository.findTopScrappedMessages(
                PageRequest.of(0, size)
        );
    }
}
