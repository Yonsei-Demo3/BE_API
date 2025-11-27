package com.backend.question.dto.response;

public record QuestionMembersResponseDTO(
        int totalMembers
) {

    public record QuestionMemberDTO(
            Long memberId,
            String nickname,
            String imageUrl
    ) {}
}
