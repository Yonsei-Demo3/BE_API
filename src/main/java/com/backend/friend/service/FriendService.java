package com.backend.friend.service;

import com.backend.friend.dto.FollowCountDTO;
import com.backend.friend.dto.FriendUserDTO;
import com.backend.friend.dto.FollowResponseDTO;
import com.backend.friend.repository.FriendRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.security.auth.exception.AuthError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    private Member getMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));
    }

    /**
     * 언팔로우 (친구 삭제)
     */
    public FollowResponseDTO unfollow(String meUserId, String targetUserId) {
        Member me = getMemberByUserId(meUserId);
        Member target = getMemberByUserId(targetUserId);

        friendRepository.deleteByFromMemberAndToMember(me, target);
        return new FollowResponseDTO(targetUserId, false);
    }

    /**
     * 내가 이 유저를 팔로우하는지 여부
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(String meUserId, String targetUserId) {
        Member me = getMemberByUserId(meUserId);
        Member target = getMemberByUserId(targetUserId);
        return friendRepository.existsByFromMemberAndToMember(me, target);
    }

    /**
     * 팔로워/팔로잉 카운트
     */
    @Transactional(readOnly = true)
    public FollowCountDTO getFollowCounts(String userId) {
        Member member = getMemberByUserId(userId);
        long following = friendRepository.countByFromMember(member);
        long follower = friendRepository.countByToMember(member);
        return new FollowCountDTO(follower, following);
    }

    /**
     * 내가 팔로우하는 사람들 리스트
     */
    @Transactional(readOnly = true)
    public List<FriendUserDTO> getFollowings(String userId) {
        Member me = getMemberByUserId(userId);
        return friendRepository.findByFromMember(me).stream()
                .map(f -> {
                    Member m = f.getToMember();
                    return new FriendUserDTO(
                            m.getUserId(),
                            m.getNickname(),
                            m.getProfile()   // 프로필 이미지 필드명에 맞게 수정
                    );
                })
                .toList();
    }

    /**
     * 나를 팔로우하는 사람들 리스트
     */
    @Transactional(readOnly = true)
    public List<FriendUserDTO> getFollowers(String userId) {
        Member me = getMemberByUserId(userId);
        return friendRepository.findByToMember(me).stream()
                .map(f -> {
                    Member m = f.getFromMember();
                    return new FriendUserDTO(
                            m.getUserId(),
                            m.getNickname(),
                            m.getProfile()
                    );
                })
                .toList();
    }
}
