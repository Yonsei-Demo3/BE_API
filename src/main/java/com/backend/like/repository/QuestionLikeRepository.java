package com.backend.like.repository;

import com.backend.like.domain.QuestionLike;
import com.backend.member.domain.Member;
import com.backend.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long> {

    boolean existsByQuestionAndMember(Question question, Member member);

    Optional<QuestionLike> findByQuestionAndMember(Question question, Member member);

    long countByQuestion(Question question);

    void deleteByQuestionAndMember(Question question, Member member);

    long countByQuestion_Id(Long questionId);

    boolean existsByQuestion_IdAndMember_UserId(Long questionId, String userId);
}
