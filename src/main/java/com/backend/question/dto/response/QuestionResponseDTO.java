package com.backend.question.dto.response;

import com.backend.category.domain.Category;
import com.backend.question.domain.Question;
import com.backend.tag.Tag;

import java.util.List;

//TODO: 참여인원수, 좋아요
public record QuestionResponseDTO(
        Long questionId,
        Long roomId,
        String questionTitle,
        String questionDescription,
        String hostNickname,
        String contentName,
        String mainCategory,
        String subCategory,
        List<String> tagNames)
{
    public static QuestionResponseDTO from(Question question, Category subCategory, List<Tag> tags) {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();
        String subCategoryName = subCategory.getName();
        // 3. (안전하게) 부모가 null인지 체크
        String mainCategoryName = (subCategory.getParent() != null)
                ? subCategory.getParent().getName()
                : "N/A";

        return new QuestionResponseDTO(
                question.getId(),
                question.getRoom().getId(),
                question.getTitle(),
                question.getDescription(),
                question.getHost().getNickname(),
                question.getContent().getName(),
                mainCategoryName,
                subCategoryName,
                tagNames
        );
    }
}
