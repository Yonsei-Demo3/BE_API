package com.backend.categoryContent;

import com.backend.category.domain.Category;
import com.backend.content.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryContentRepository extends JpaRepository<CategoryContent, Long> {
    List<CategoryContent> findAllByContentIn(List<Content> contents);

    List<CategoryContent> findByContentIdIn(List<Long> contentIds);
}
