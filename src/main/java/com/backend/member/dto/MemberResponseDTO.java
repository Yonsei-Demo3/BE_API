package com.backend.member.dto;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;

public record MemberResponseDTO(
        String userId,
        String email,
        String nickname,
        Role role
) {
    public static MemberResponseDTO from(Member m) {
        return new MemberResponseDTO(
                m.getUserId(),
                m.getEmail(),
                m.getNickname(),
                m.getRole()
        );
    }
}
