package com.backend.question.repository;

import com.backend.question.domain.Question;
import com.backend.question.dto.request.QuestionSearchRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {

    Page<Question> search(QuestionSearchRequestDTO dto, Pageable pageable);
}
