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
