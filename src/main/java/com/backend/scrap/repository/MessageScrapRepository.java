package com.backend.scrap.repository;

import com.backend.member.domain.Member;
import com.backend.message.domain.Message;
import com.backend.scrap.domain.MessageScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageScrapRepository extends JpaRepository<MessageScrap, Long> {

    boolean existsByMemberAndMessage(Member member, Message message);

    Optional<MessageScrap> findByMemberAndMessage(Member member, Message message);

    List<MessageScrap> findByMemberOrderByIdDesc(Member member);
}
