package com.backend.message;

import com.backend.chatroom.domain.Chatroom;
import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
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
    @JoinColumn(name = "chatroom_id", nullable = false) // 9. 'chatroom_id'라는 FK 컬럼이 생성됨
    private Chatroom chatroom;

    //TODO: 양방향 관계 고민

    @Builder
    private Message(MessageType type, String content, Member member, Chatroom chatroom) {
        this.type = type;
        this.content = content;
        this.member = member;
        this.chatroom = chatroom;
    }

    public static Message of(MessageType type, String content, Member member, Chatroom chatroom) {
        return new Message(type, content, member, chatroom);
    }



}
