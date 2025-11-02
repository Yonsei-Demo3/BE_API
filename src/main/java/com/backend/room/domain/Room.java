package com.backend.room.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus roomStatus;

    //TODO: Message도 양방향으로 추가하는 게 좋을 듯...? Base Entitiy 추가

    @Builder
    private Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Room of(String name, String description) {
        return new Room(name, description);
    }
}
