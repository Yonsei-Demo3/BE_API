package com.backend.question.domain;

import com.backend.content.domain.Content;
import com.backend.member.domain.Member;
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

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants",nullable = false)
    private int currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    @ManyToOne(fetch = FetchType.LAZY) //TODO: Cascade 정책 고려...
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne
    @JoinColumn(name = "parent_question_id")
    private Question parentQuestion;

    public void increaseCurrentParticipants() {
        this.currentParticipants++;
    }

    @Builder
    private Question(String title, String description, int maxParticipants, QuestionStatus status, Member host, Room room, Content content, Question parentQuestion) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.host = host;
        this.room = room;
        this.content = content;
        this.parentQuestion = parentQuestion;
    }

    public static Question createFirstQuestionOf(String title, String description,int maxParticipants, Member host, Room room, Content content) {

        return Question.builder()
                .title(title)
                .description(description)
                .maxParticipants(maxParticipants)
                .status(QuestionStatus.PREPARING)
                .content(content)
                .host(host)
                .room(room)
                .parentQuestion(null)
                .build();
    }
}
