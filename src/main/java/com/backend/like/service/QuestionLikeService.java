package com.backend.like.service;

import com.backend.like.domain.QuestionLike;
import com.backend.like.repository.QuestionLikeRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.question.domain.Question;
import com.backend.question.repository.QuestionRepository;
import com.backend.security.auth.exception.AuthError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionLikeService {

    private final QuestionLikeRepository likeRepository;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;

    /**
     * 좋아요 추가 (idempotent: 이미 눌러져 있으면 그냥 카운트만 반환)
     */
    @Transactional
    public long likeQuestion(String userId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. id=" + questionId));

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("invalid_user", "존재하지 않는 회원입니다."));

        if (!likeRepository.existsByQuestionAndMember(question, member)) {
            likeRepository.save(new QuestionLike(question, member));
        }

        return likeRepository.countByQuestion(question);
    }

    /**
     * 좋아요 취소
     */
    @Transactional
    public long unlikeQuestion(String userId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. id=" + questionId));

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("invalid_user", "존재하지 않는 회원입니다."));

        likeRepository.deleteByQuestionAndMember(question, member);

        return likeRepository.countByQuestion(question);
    }

    /**
     * 특정 질문의 좋아요 수
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. id=" + questionId));

        return likeRepository.countByQuestion(question);
    }

    /**
     * 현재 유저가 좋아요 눌렀는지 여부
     */
    @Transactional(readOnly = true)
    public boolean hasLiked(String userId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. id=" + questionId));

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("invalid_user", "존재하지 않는 회원입니다."));

        return likeRepository.existsByQuestionAndMember(question, member);
    }
}
