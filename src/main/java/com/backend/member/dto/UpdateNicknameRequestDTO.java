package com.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequestDTO(
        @NotBlank @Size(min = 2, max = 50) String nickname
) {}
