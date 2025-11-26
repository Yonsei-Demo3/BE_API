package com.backend.categoryContent;

import com.backend.content.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryContentRepository extends JpaRepository<CategoryContent, Long> {
    List<CategoryContent> findAllByContentIn(List<Content> contents);
    Optional<CategoryContent> findByContent(Content content);

    List<CategoryContent> findByContentIdIn(List<Long> contentIds);
}
