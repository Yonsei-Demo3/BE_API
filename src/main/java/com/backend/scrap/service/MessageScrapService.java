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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageScrapService {

    private final MessageScrapRepository messageScrapRepository;
    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;

    /**
     * 메시지 스크랩 추가 (이미 스크랩 상태면 그대로 유지 - idempotent)
     */
    public MessageScrapResponseDTO scrap(String userId, Long messageId) {
        Member me = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메시지입니다."));

        return messageScrapRepository.findByMemberAndMessage(me, message)
                .map(scrap -> new MessageScrapResponseDTO(
                        message.getId(),
                        true,
                        scrap.getScrappedAt()
                ))
                .orElseGet(() -> {
                    MessageScrap saved = messageScrapRepository.save(MessageScrap.of(me, message));
                    return new MessageScrapResponseDTO(
                            message.getId(),
                            true,
                            saved.getScrappedAt()
                    );
                });
    }

    /**
     * 스크랩 취소 (없어도 에러 내지 않고 조용히 처리)
     */
    public MessageScrapResponseDTO unscrap(String userId, Long messageId) {
        Member me = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메시지입니다."));

        messageScrapRepository.findByMemberAndMessage(me, message)
                .ifPresent(messageScrapRepository::delete);

        // scrappedAt 은 이제 의미 없으니 null
        return new MessageScrapResponseDTO(message.getId(), false, null);
    }

    /**
     * 내 스크랩 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ScrapMessageDTO> getMyScraps(String userId) {
        Member me = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));

        List<MessageScrap> scraps = messageScrapRepository.findByMemberOrderByIdDesc(me);

        return scraps.stream()
                .map(scrap -> {
                    Message m = scrap.getMessage();
                    Long roomId = (m.getRoom() != null) ? m.getRoom().getId() : null;
                    String content = m.getContent(); // Message 엔티티에 맞게 필드명 조정
                    return new ScrapMessageDTO(
                            scrap.getId(),
                            m.getId(),
                            roomId,
                            content,
                            scrap.getScrappedAt()
                    );
                })
                .toList();
    }

    /**
    * 특정 사용자의 스크랩 목록 조회 (친구 관계 검증 포함)
    */
    @Transactional(readOnly = true)
    public List<ScrapMessageDTO> getUserScraps(String targetUserId) {

    // 타겟 사용자 조회
    Member target = memberRepository.findByUserId(targetUserId)
            .orElseThrow(() -> new AuthError("not_found_target_member", "조회 대상 사용자가 존재하지 않습니다."));

    // 스크랩 조회
    List<MessageScrap> scraps = messageScrapRepository.findByMemberOrderByIdDesc(target);

    return scraps.stream()
        .map(scrap -> {
                Message m = scrap.getMessage();
                Long roomId = (m.getRoom() != null) ? m.getRoom().getId() : null;

                return new ScrapMessageDTO(
                        scrap.getId(),
                        m.getId(),
                        roomId,
                        m.getContent(),
                        scrap.getScrappedAt()
                );
        })
        .toList();
    }
}