package com.backend.categoryContent;

import com.backend.category.domain.Category;
import com.backend.content.domain.Content;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//TODO: 유니크 제약 조건을 걸어야 할까?
@Entity
@Table(name = "category_contents")
@NoArgsConstructor
@Getter
public class CategoryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Builder
    private CategoryContent(Category category, Content content) {
        this.category = category;
        this.content = content;
    }

    public static CategoryContent of(Category category, Content content) {
        return new CategoryContent(category, content);
    }

}
