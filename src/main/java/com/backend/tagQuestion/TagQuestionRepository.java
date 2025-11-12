package com.backend.tagQuestion;

import com.backend.question.domain.Question;
import com.backend.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagQuestionRepository extends JpaRepository<TagQuestion, Long> {
    List<TagQuestion> findAllByQuestion(Question question);
    List<TagQuestion> findAllByQuestionIn(List<Question> questions);

}
