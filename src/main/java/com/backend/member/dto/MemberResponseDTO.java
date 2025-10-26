package com.backend.member.dto;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;

public record MemberResponseDTO(
        Long id,
        String email,
        String nickname,
        Role role
) {
    public static MemberResponseDTO from(Member m) {
        return new MemberResponseDTO(m.getId(), m.getEmail(), m.getNickname(), m.getRole());
    }
}
