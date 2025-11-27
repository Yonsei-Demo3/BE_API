package com.backend.question.dto.response;

import com.backend.category.domain.Category;
import com.backend.question.domain.Question;
import com.backend.question.domain.QuestionStatus;
import com.backend.question.domain.ParticipationStatus;
import com.backend.tag.Tag;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

//TODO: 참여인원수, 좋아요
public record QuestionResponseDTO(
        Long questionId,
        Long roomId,
        Long hostId,
        String questionTitle,
        String questionDescription,
        QuestionStatus questionStatus,
        String hostNickname,
        String contentName,
        String mainCategory,
        String subCategory,
        String imageUrl,

        Integer maxParticipants,
        Integer currentParticipants,
        List<String> tagNames,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        ParticipationStatus myParticipationStatus,
        Integer likeCount,
        Boolean isLikedByMe
        )
{
    public static QuestionResponseDTO from(
                Question question,
                Category subCategory,
                List<Tag> tags,
                ParticipationStatus myStatus,
                Integer likeCount,
                Boolean isLikedByMe
        ) {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();

        String subCategoryName = "N/A";
        String mainCategoryName = "N/A";

        if (subCategory != null) {
            subCategoryName = subCategory.getName();
            mainCategoryName = (subCategory.getParent() != null)
                    ? subCategory.getParent().getName()
                    : "N/A"; // 부모가 없는 경우 (최상위 카테고리인 경우)
        }

        return new QuestionResponseDTO(
                question.getId(),
                question.getRoom().getId(),
                question.getHost().getId(),
                question.getTitle(),
                question.getDescription(),
                question.getStatus(),
                question.getHost().getNickname(),
                question.getContent().getName(),
                mainCategoryName,
                subCategoryName,
                question.getContent().getImageUrl(),
                question.getMaxParticipants(),
                question.getCurrentParticipants(),
                tagNames,
                question.getCreatedAt(),
                myStatus,
                likeCount,
                isLikedByMe
        );
    }
}
