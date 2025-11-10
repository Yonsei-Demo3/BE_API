package com.backend.category.service;

import com.backend.category.CategoryProperties;
import com.backend.category.domain.Category;
import com.backend.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryProperties categoryProperties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initCategories() {
        for (CategoryProperties.CategoryNode rootNode : categoryProperties.getCategories()) {

            // TODO: children 변경 여부로 if문 판단
            if (categoryRepository.findByName(rootNode.getName()).isEmpty()) {

                // DB에 저장
                Category rootCategory = Category.createRoot(rootNode.getName());
                categoryRepository.save(rootCategory);

                //
                for (String childName : rootNode.getChildren()) {
                    // (하위 카테고리도 중복 체크)
                    if (categoryRepository.findByName(childName).isEmpty()) {
                        Category childCategory = Category.createChild(childName, rootCategory);
                        categoryRepository.save(childCategory);
                    }
                }
            }
        }
        System.out.println("Category data seeding from YML completed.");
    }
}
