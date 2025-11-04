package com.backend.question.controller;

import com.backend.question.QuestionService;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.security.auth.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<CreateQuestionResponseDTO> createFirstQuestion(@AuthenticationPrincipal CustomUserPrincipal me, CreateFirstQuestionRequestDTO dto) {
        CreateQuestionResponseDTO responseDTO = questionService.createFirstQuestion(me.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
