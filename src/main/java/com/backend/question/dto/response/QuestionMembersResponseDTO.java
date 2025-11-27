package com.backend.question.dto.response;

import com.backend.member.domain.Member;

import java.util.List;

public record QuestionMembersResponseDTO(
        int totalMembers,
        List<QuestionMemberDTO> members
) {
    public static QuestionMembersResponseDTO of(List<Member> members, Long currentMemberId) {
        List<QuestionMemberDTO> dtos = members.stream()
                .map(member -> QuestionMemberDTO.from(member, currentMemberId))
                .toList();
        return new QuestionMembersResponseDTO(dtos.size(), dtos);

    }

    //TODO: 프론트에서 memberID 전역변수로 처리하는 방법으로 수정
    public record QuestionMemberDTO(
            Long memberId,
            String nickname,
            String imageUrl,
            boolean isMe
    ) {
        public static QuestionMemberDTO from(Member member, Long currentMemberId) {

            return new QuestionMemberDTO(
                    member.getId(),
                    member.getNickname(),
                    member.getProfile(),
                    member.getId().equals(currentMemberId)
            );
        }
    }
}
