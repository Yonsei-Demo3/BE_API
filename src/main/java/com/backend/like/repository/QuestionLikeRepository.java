package com.backend.like.repository;

import com.backend.like.domain.QuestionLike;
import com.backend.member.domain.Member;
import com.backend.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.backend.like.dto.QuestionLikeSummary;

import java.util.List;

public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long> {

    boolean existsByQuestionAndMember(Question question, Member member);

    void deleteByQuestionAndMember(Question question, Member member);

    long countByQuestion_Id(Long questionId);

    boolean existsByQuestion_IdAndMember_UserId(Long questionId, String userId);

    @Query("""
        SELECT ql.question.id AS questionId,
               COUNT(allLikes.id) AS likeCount
        FROM QuestionLike ql
        JOIN QuestionLike allLikes
            ON ql.question.id = allLikes.question.id
        WHERE ql.member.userId = :userId
        GROUP BY ql.question.id
        """)
    List<QuestionLikeSummary> findLikedQuestionIdsWithCountsByUserId(@Param("userId") String userId);
}
