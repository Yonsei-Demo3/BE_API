package com.backend.content.controller;

import com.backend.content.dto.ContentCreateRequestDTO;
import com.backend.content.dto.ContentResponseDTO;
import com.backend.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<ContentResponseDTO> createContent(@RequestBody ContentCreateRequestDTO dto) {
        ContentResponseDTO responseDTO = contentService.createContent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
