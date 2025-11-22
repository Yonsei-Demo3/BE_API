package com.backend.scrap.domain;

import com.backend.member.domain.Member;
import com.backend.message.domain.Message;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "message_scraps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_scrap_member_message",
                        columnNames = {"member_id", "message_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 스크랩한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 스크랩 대상 메시지(댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "scrapped_at", nullable = false)
    private LocalDateTime scrappedAt;

    private MessageScrap(Member member, Message message) {
        this.member = member;
        this.message = message;
        this.scrappedAt = LocalDateTime.now();
    }

    public static MessageScrap of(Member member, Message message) {
        return new MessageScrap(member, message);
    }
}
