package com.backend.S3.controller;

import com.backend.S3.dto.FileUploadRequestDTO;
import com.backend.S3.dto.FileUploadResponseDTO;
import com.backend.S3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/presigned")
    public ResponseEntity<FileUploadResponseDTO> createPresignedUrl(
            @RequestBody FileUploadRequestDTO request
    ) {

        String uploadUrl = s3Service.generateUploadUrl(request.fileName(), request.contentType());
        // 버킷명이 뭘까?
        String fileUrl = "https:/sai_project.s3.ap-northeast-2.amazonaws.com/" + request.fileName();

        return ResponseEntity.ok(
                new FileUploadResponseDTO(uploadUrl, fileUrl)
        );
    }
}
