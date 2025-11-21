package com.backend.like.dto;

/**
 * DTO representing the like status of a question.
 *
 * @param questionId The ID of the question
 * @param likeCount Total number of likes for this question
 * @param likedByMe Whether the current user has liked the question (false if unauthenticated)
 */
public record QuestionLikeResponseDTO(
        Long questionId,
        long likeCount,
        boolean likedByMe
) {}
