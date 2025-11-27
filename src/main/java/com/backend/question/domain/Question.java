package com.backend.question.domain;

import com.backend.content.domain.Content;
import com.backend.member.domain.Member;
import com.backend.room.domain.Room;
import global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private int currentParticipants = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStartMode startMode;

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

    public void decreaseCurrentParticipants() {
        if (this.currentParticipants <= 1) {
            throw new IllegalStateException("Cannot decrease participants below minimum count");
        }
        this.currentParticipants--;
    }

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private int likeCount = 0;

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }


    public void recruitingToReadyCheck() {
        if (this.status != QuestionStatus.RECRUITING) {
            throw new IllegalStateException("Question must be in RECRUITING status to transition to READY_CHECK");
        }
        this.status = QuestionStatus.READY_CHECK;
    }

    public void readyCheckToActive() {
        if (this.status != QuestionStatus.READY_CHECK) {
            throw new IllegalStateException("Question must be in READY_CHECK status to become ACTIVE");
        }
        this.status = QuestionStatus.ACTIVE;
    }

    public void activeToFinished() {
        if (this.status != QuestionStatus.ACTIVE) {
            throw new IllegalStateException("Question must be in ACTIVE status to become FINISHED");
        }
        this.status = QuestionStatus.FINISHED;
    }

    @Builder
    private Question(String title, String description, int maxParticipants, QuestionStartMode startMode, QuestionStatus status, Member host, Room room, Content content, Question parentQuestion) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.startMode = startMode;
        this.status = status;
        this.host = host;
        this.room = room;
        this.content = content;
        this.parentQuestion = parentQuestion;
    }

    public static Question createFirstQuestionOf(String title, String description, int maxParticipants, QuestionStartMode startMode, Member host, Room room, Content content) {

        return Question.builder()
                .title(title)
                .description(description)
                .maxParticipants(maxParticipants)
                .startMode(startMode)
                .status(QuestionStatus.RECRUITING)
                .content(content)
                .host(host)
                .room(room)
                .parentQuestion(null)
                .build();
    }
}
