package com.backend.S3.dto;

public record FileUploadResponseDTO(
    String uploadUrl,
    String fileUrl
) {}
