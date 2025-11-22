package com.backend.S3.dto;

public record FileUploadRequestDTO(
    String fileName,
    String contentType
) {}
