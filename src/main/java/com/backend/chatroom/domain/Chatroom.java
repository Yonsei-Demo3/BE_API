package com.backend.chatroom.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
public class Chatroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Builder
    private Chatroom(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Chatroom of(String name, String description) {
        return new Chatroom(name, description);
    }
}
