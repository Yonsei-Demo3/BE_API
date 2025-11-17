package com.backend.tagQuestion;

import com.backend.question.domain.Question;
import com.backend.tag.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag_questions", // 2. (필수) DB 테이블 이름 명시
        uniqueConstraints = {
                // 3. (★매우 중요★) 하나의 질문에 '같은 태그'가 두 번 붙는 것을 DB에서 원천 차단
                @UniqueConstraint(
                        name = "question_tag_uk",
                        columnNames = {"question_id", "tag_id"}
                )
        }
)
@NoArgsConstructor
@Getter
public class TagQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private TagQuestion(Tag tag, Question question) {
        this.tag = tag;
        this.question = question;
    }

    public static TagQuestion of(Tag tag, Question question) {
        return new TagQuestion(tag, question);
    }
}
