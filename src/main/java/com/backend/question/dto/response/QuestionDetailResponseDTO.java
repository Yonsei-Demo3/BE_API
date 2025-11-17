package com.backend.question.dto.response;

import com.backend.question.domain.Question;
import com.backend.tag.Tag;

import java.util.List;

//TODO: 멤버 정보는 닉네임만 넘겨주면 되는건가
public record QuestionDetailResponseDTO(
        Long questionId,
        String contentName,
        List<String> tags,
        String questionTitle,
        String description,
        String hostNickname,
        String imageUrl
) {
    public static QuestionDetailResponseDTO from(Question question, List<Tag> tags) {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();

        return new QuestionDetailResponseDTO(question.getId(), question.getContent().getName(),tagNames, question.getTitle(), question.getDescription(), question.getHost().getNickname(), question.getContent().getImageUrl());
    }
}
