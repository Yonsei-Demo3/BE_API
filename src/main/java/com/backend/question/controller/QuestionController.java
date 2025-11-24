package com.backend.question.controller;

import com.backend.question.dto.request.QuestionSearchRequestDTO;
import com.backend.question.dto.response.QuestionDTO;
import com.backend.question.dto.response.QuestionDetailResponseDTO;
import com.backend.question.service.QuestionService;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.security.auth.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.backend.search.service.RecentSearchService;
import com.backend.search.service.PopularSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final RecentSearchService recentSearchService;
    private final PopularSearchService popularSearchService;

    @PostMapping
    public ResponseEntity<CreateQuestionResponseDTO> createFirstQuestion(@AuthenticationPrincipal CustomUserPrincipal me, @RequestBody CreateFirstQuestionRequestDTO dto) {
        CreateQuestionResponseDTO responseDTO = questionService.createFirstQuestion(me.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> participateQuestion(@AuthenticationPrincipal CustomUserPrincipal me, @PathVariable Long questionId) {
        QuestionDTO responseDTO = questionService.participateQuestion(me.getUserId(), questionId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponseDTO>> getMyQuestions(@AuthenticationPrincipal CustomUserPrincipal me) {
        List<QuestionResponseDTO> responseDTOS = questionService.getQuestions(me.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(responseDTOS);

    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailResponseDTO> getQuestionDetailById(@PathVariable Long questionId) {
        QuestionDetailResponseDTO responseDTO = questionService.getQuestionDetailById(questionId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }


    @PostMapping("/search")
    public ResponseEntity<Page<QuestionResponseDTO>> searchQuestions(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody QuestionSearchRequestDTO questionSearchRequestDTO,
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        String keyword = questionSearchRequestDTO.keyword();

        if (me != null &&
            keyword != null &&
            !keyword.isBlank()) {

            recentSearchService.addKeyword(
                    String.valueOf(me.getUserId()),
                    keyword
            );
        }
        if (keyword != null && !keyword.isBlank()) {
            popularSearchService.increase(keyword);
        }

        Page<QuestionResponseDTO> responses =  questionService.searchQuestions(questionSearchRequestDTO, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
