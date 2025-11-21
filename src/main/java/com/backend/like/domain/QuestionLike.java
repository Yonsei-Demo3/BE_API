package com.backend.like.domain;

import com.backend.member.domain.Member;
import com.backend.question.domain.Question;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "question_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_question_like",
                columnNames = {"question_id", "member_id"}
        )
)
@Getter
@NoArgsConstructor
public class QuestionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 질문에 대한 좋아요일까?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 누가 좋아요를 눌렀는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public QuestionLike(Question question, Member member) {
        this.question = question;
        this.member = member;
    }
}
