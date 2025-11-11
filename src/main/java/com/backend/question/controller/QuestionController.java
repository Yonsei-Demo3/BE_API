package com.backend.question.controller;

import com.backend.question.dto.response.QuestionDetailResponseDTO;
import com.backend.question.service.QuestionService;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.security.auth.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<CreateQuestionResponseDTO> createFirstQuestion(@AuthenticationPrincipal CustomUserPrincipal me, @RequestBody CreateFirstQuestionRequestDTO dto) {
        CreateQuestionResponseDTO responseDTO = questionService.createFirstQuestion(me.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDTO> participateQuestion(@AuthenticationPrincipal CustomUserPrincipal me, @PathVariable Long questionId) {
        QuestionResponseDTO responseDTO = questionService.participateQuestion(me.getUserId(), questionId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailResponseDTO> getQuestionDetailById(@PathVariable Long questionId) {
        QuestionDetailResponseDTO responseDTO = questionService.getQuestionDetailById(questionId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }
}
