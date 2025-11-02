package com.backend.question;

import com.backend.content.Content;
import com.backend.room.domain.Room;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    //TODO: Who made this? 고려해야하나....(방장) 방장 정보를 어디에 담을까!!!! Room, RoomMember, Question

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY) //TODO: Cascade 정책 고려...
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne
    @JoinColumn(name = "parent_question_id")
    private Question parentQuestion;


    @Builder
    private Question(String title, String description, int maxParticipants, Room room, Content content, Question parentQuestion) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.room = room;
        this.content = content;
        this.parentQuestion = parentQuestion;
    }
}
