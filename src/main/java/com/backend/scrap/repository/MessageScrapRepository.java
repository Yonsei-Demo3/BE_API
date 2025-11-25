package com.backend.scrap.repository;

import com.backend.member.domain.Member;
import com.backend.message.domain.Message;
import com.backend.scrap.dto.ScrappedMessageSummaryDTO;
import com.backend.scrap.dto.ScrapMessageDTO;
import com.backend.scrap.domain.MessageScrap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageScrapRepository extends JpaRepository<MessageScrap, Long> {

    boolean existsByMemberAndMessage(Member member, Message message);

    Optional<MessageScrap> findByMemberAndMessage(Member member, Message message);

    List<MessageScrap> findByMemberOrderByIdDesc(Member member);

    @Query("""
        SELECT new com.backend.scrap.dto.ScrappedMessageSummaryDTO(
            ms.message.id,
            COUNT(ms.id)
        )
        FROM MessageScrap ms
        GROUP BY ms.message.id
        ORDER BY COUNT(ms.id) DESC
    """)
    List<ScrappedMessageSummaryDTO> findTopScrappedMessages(Pageable pageable);

    @Query("""
    select new com.backend.scrap.dto.ScrapMessageDTO(
        m.id,
        m.content,
        q.id,
        q.title,
        c.id,
        c.name,
        s.scrappedAt
    )
    from MessageScrap s
        join s.message m
        join Question q on q.room = m.room
        left join q.content c
    where s.member.userId = :userId
    order by s.scrappedAt desc
    """)
    List<ScrapMessageDTO> findScrapsByUserId(@Param("userId") String userId);
}
