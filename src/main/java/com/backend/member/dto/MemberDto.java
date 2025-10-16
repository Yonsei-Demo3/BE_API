package com.backend.member.dto;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberDto {

    public record SignUpRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(min = 2, max = 50) String nickname
    ) {}

    public record UpdateNicknameRequest(
            @NotBlank @Size(min = 2, max = 50) String nickname
    ) {}

    public record Response(
            Long id, String email, String nickname, Role role
    ) {
        public static Response from(Member m) {
            return new Response(m.getId(), m.getEmail(), m.getNickname(), m.getRole());
        }
    }
}