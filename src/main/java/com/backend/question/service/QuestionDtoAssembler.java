package com.backend.question.service;

import com.backend.category.domain.Category;
import com.backend.categoryContent.CategoryContent;
import com.backend.categoryContent.CategoryContentRepository;
import com.backend.content.domain.Content;
import com.backend.question.domain.Question;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.tag.Tag;
import com.backend.tagQuestion.TagQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuestionDtoAssembler {

    private final TagQuestionRepository tagQuestionRepository;
    private final CategoryContentRepository categoryContentRepository;

    public Page<QuestionResponseDTO> toPageDto(Page<Question> questionPage) {
        List<Question> questions = questionPage.getContent();

        Map<Long, List<Tag>> questionTagMap = getQuestionTagMap(questions);
        Map<Long, Category> contentCategoryMap = getContentCategoryMap(questions);

        return questionPage.map(question -> {
            List<Tag> tags = questionTagMap.getOrDefault(question.getId(), Collections.emptyList());
            Category category = contentCategoryMap.get(question.getContent().getId());
            return QuestionResponseDTO.from(question, category, tags);
        });
    }

    private Map<Long, List<Tag>> getQuestionTagMap(List<Question> questions) {
        return tagQuestionRepository
                .findAllByQuestionIn(questions)
                .stream()
                .collect(Collectors.groupingBy(
                        qt -> qt.getQuestion().getId(),
                        Collectors.mapping(qt -> qt.getTag(), Collectors.toList())
                ));
    }

    private Map<Long, Category> getContentCategoryMap(List<Question> questions) {
        List<Content> contents = questions.stream()
                .map(Question::getContent)
                .distinct()
                .toList();

        Map<Long, List<CategoryContent>> contentCategoryContentMap = categoryContentRepository
                .findAllByContentIn(contents)
                .stream()
                .collect(Collectors.groupingBy(cc -> cc.getContent().getId()));

        return contentCategoryContentMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(CategoryContent::getCategory)
                                .findFirst()
                                .orElse(null)
                ));
    }
}
