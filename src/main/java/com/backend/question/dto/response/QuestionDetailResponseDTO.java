package com.backend.question.dto.response;

import com.backend.category.domain.Category;
import com.backend.question.domain.Question;
import com.backend.question.domain.ParticipationStatus; 
import com.backend.tag.Tag;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

//TODO: 멤버 정보는 닉네임만 넘겨주면 되는건가
public record QuestionDetailResponseDTO(
        Long questionId,
        String contentName,
        String questionStatus,
        String startMode,
        List<String> tags,
        String questionTitle,
        String description,
        String hostNickname,
        String imageUrl,
        String mainCategory,
        String subCategory,
        Integer maxParticipants,
        Integer currentParticipants,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        ParticipationStatus myParticipationStatus
) {
    public static QuestionDetailResponseDTO from(Question question, List<Tag> tags, Category mainCategory, Category subCategory, ParticipationStatus myParticipationStatus) {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();

        return new QuestionDetailResponseDTO(
                question.getId(),
                question.getContent().getName(),
                question.getStatus().name(),
                question.getStartMode().name(),
                tagNames,
                question.getTitle(),
                question.getDescription(),
                question.getHost().getNickname(),
                question.getContent().getImageUrl(),
                mainCategory.getName(),
                subCategory.getName(),
                question.getMaxParticipants(),
                question.getCurrentParticipants(),
                question.getCreatedAt(),
                myParticipationStatus);
    }
}
