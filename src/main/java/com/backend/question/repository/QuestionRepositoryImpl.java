package com.backend.question.repository;

import com.backend.question.domain.Question;
import com.backend.question.dto.request.QuestionSearchRequestDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;


import java.util.List;

import static com.backend.category.domain.QCategory.category;
import static com.backend.categoryContent.QCategoryContent.categoryContent;
import static com.backend.content.domain.QContent.content;
import static com.backend.question.domain.QQuestion.question;
import static com.backend.tag.QTag.tag;
import static com.backend.tagQuestion.QTagQuestion.tagQuestion;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {


    private final JPAQueryFactory queryFactory;

    //책임 분리 vs 성능
    @Override
    public Page<Question> search(QuestionSearchRequestDTO dto, Pageable pageable) {

        // 1. 데이터 조회 쿼리
        List<Question> results = queryFactory
                .select(question).distinct()
                .from(question)
                .leftJoin(question.host).fetchJoin()
                .leftJoin(question.content, content)
                .leftJoin(categoryContent).on(categoryContent.content.eq(content))
                .leftJoin(categoryContent.category, category)
                .leftJoin(tagQuestion).on(tagQuestion.question.eq(question))
                .leftJoin(tagQuestion.tag, tag)
                .where(
                        containsKeyword(dto.keyword()),
                        inCategories(dto.categories()),
                        inTags(dto.tags())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(question.id.desc())
                .fetch();

        // 2. 전체 카운트 조회 쿼리
        Long total = queryFactory
                .select(question.id.countDistinct())
                .from(question)
                .leftJoin(question.content, content)
                .leftJoin(categoryContent).on(categoryContent.content.eq(content))
                .leftJoin(categoryContent.category, category)
                .leftJoin(tagQuestion).on(tagQuestion.question.eq(question))
                .leftJoin(tagQuestion.tag, tag)
                .where(
                        containsKeyword(dto.keyword()),
                        inCategories(dto.categories()),
                        inTags(dto.tags())
                )
                .fetchOne();

        // 3. Page 객체로 변환하여 반환
        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }
    //title LIKE '%keyword%'
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return question.title.containsIgnoreCase(keyword)
                .or(question.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression inTags(List<String> tagNames) {
        if(tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        return tag.name.in(tagNames); // (WHERE tag.name IN ('spring', 'jpa'))
    }

    private Predicate inCategories(List<QuestionSearchRequestDTO.CategorySearchPair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return null;
        }

        BooleanBuilder builder = new BooleanBuilder();
        for (QuestionSearchRequestDTO.CategorySearchPair pair : pairs) {
            BooleanExpression subCategoryEq = category.name.eq(pair.sub());
            // (대분류 이름 - 'parent'를 통해 JOIN)
            BooleanExpression mainCategoryEq = category.parent.name.eq(pair.main());

            builder.or(subCategoryEq.and(mainCategoryEq));
        }

        return builder.getValue();
    }
}
