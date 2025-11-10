package com.backend.message.domain;

import com.backend.room.domain.Room;
import com.backend.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false) // 9. 'room_id'라는 FK 컬럼이 생성됨
    private Room room;

    //TODO: BaseEntity 추가



    @Builder
    private Message(MessageType type, String content, Member member, Room room) {
        this.type = type;
        this.content = content;
        this.member = member;
        this.room = room;
    }

    public static Message of(MessageType type, String content, Member member, Room room) {
        return new Message(type, content, member, room);
    }
}