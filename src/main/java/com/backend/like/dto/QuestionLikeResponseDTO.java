package com.backend.like.dto;

public record QuestionLikeResponseDTO(
        Long questionId,
        long likeCount,
        boolean likedByMe
) {}
