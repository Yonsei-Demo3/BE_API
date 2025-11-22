package com.backend.search.service;

import com.backend.content.repository.ContentRepository;
import com.backend.question.repository.QuestionRepository;
import com.backend.search.dto.ContentSearchItemDTO;
import com.backend.search.dto.QuestionSearchItemDTO;
import com.backend.search.dto.SearchResponseDTO;
import com.backend.search.dto.TagSearchItemDTO;
import com.backend.tag.Tag;
import com.backend.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final QuestionRepository questionRepository;
    private final ContentRepository contentRepository;
    private final TagRepository tagRepository;

    public SearchResponseDTO unifiedSearch(String rawKeyword, Pageable pageable) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return new SearchResponseDTO(List.of(), List.of(), List.of());
        }

        String keyword = rawKeyword.trim();

        // 1) 질문 검색
        var questionPage = questionRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword, keyword, pageable
                );

        List<QuestionSearchItemDTO> questionDtos = questionPage.getContent().stream()
                .map(q -> new QuestionSearchItemDTO(
                        q.getId(),
                        q.getTitle(),
                        q.getDescription()
                ))
                .toList();

        // 2) 콘텐츠 검색
        var contentPage = contentRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword,
                    keyword,
                    pageable
                );

        List<ContentSearchItemDTO> contentDtos = contentPage.getContent().stream()
                .map(c -> new ContentSearchItemDTO(
                        c.getId(),
                        c.getName(),
                        c.getCreator()
                ))
                .toList();

        // 3) 태그 검색 (상위 10개 정도만)
        List<Tag> tags = tagRepository.findTop10ByNameContainingIgnoreCase(keyword);
        List<TagSearchItemDTO> tagDtos = tags.stream()
                .map(t -> new TagSearchItemDTO(
                        t.getId(),
                        t.getName()
                ))
                .toList();

        return new SearchResponseDTO(questionDtos, contentDtos, tagDtos);
    }
}
