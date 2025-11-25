package com.backend.friend.service;

import com.backend.friend.domain.Block;
import com.backend.friend.domain.Friend;
import com.backend.friend.domain.FriendRequest;
import com.backend.friend.domain.FriendRequestStatus;
import com.backend.friend.dto.BlockResponseDTO;
import com.backend.friend.dto.FriendRequestCreateRequestDTO;
import com.backend.friend.dto.FriendRequestResponseDTO;
import com.backend.friend.dto.FriendSummaryDTO;
import com.backend.friend.repository.BlockRepository;
import com.backend.friend.repository.FriendRepository;
import com.backend.friend.repository.FriendRequestRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.security.auth.exception.AuthError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;

    private Member getMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("invalid_user", "존재하지 않는 회원입니다."));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + memberId));
    }

    /**
     * 친구 요청 보내기
     */
    @Transactional
    public FriendRequestResponseDTO sendFriendRequest(String meUserId, FriendRequestCreateRequestDTO dto) {
        Member me = getMemberByUserId(meUserId);
        Member target = getMemberById(dto.targetMemberId());

        if (me.getId().equals(target.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 친구인지 체크
        if (friendRepository.existsBetween(me, target)) {
            throw new IllegalStateException("이미 친구 관계입니다.");
        }

        // 차단 여부 체크 (내가 차단했거나 / 상대가 나를 차단했거나)
        if (blockRepository.existsByBlockerAndBlocked(me, target) ||
            blockRepository.existsByBlockerAndBlocked(target, me)) {
            throw new IllegalStateException("차단 관계에서는 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 PENDING 요청 있는지 체크 (중복 방지)
        friendRequestRepository.findByFromMemberAndToMemberAndStatus(
                me, target, FriendRequestStatus.PENDING
        ).ifPresent(fr -> {
            throw new IllegalStateException("이미 대기중인 친구 요청이 존재합니다.");
        });

        FriendRequest request = new FriendRequest(me, target, dto.message());
        FriendRequest saved = friendRequestRepository.save(request);

        return new FriendRequestResponseDTO(
                saved.getId(),
                saved.getFromMember().getId(),
                saved.getToMember().getId(),
                saved.getMessage(),
                saved.getStatus()
        );
    }

    /**
     * 친구 요청 수락
     */
    @Transactional
    public void acceptRequest(String meUserId, Long requestId) {
        Member me = getMemberByUserId(meUserId);

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다. id=" + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        if (!request.getToMember().getId().equals(me.getId())) {
            throw new AuthError("forbidden", "해당 친구 요청을 수락할 권한이 없습니다.");
        }

        // 친구 관계 생성
        Friend friend = new Friend(request.getFromMember(), request.getToMember());
        if (!friendRepository.existsBetween(request.getFromMember(), request.getToMember())) {
            friendRepository.save(friend);
        }

        // 요청 상태 변경
        request.accept();
    }

    /**
     * 친구 요청 거절
     */
    @Transactional
    public void rejectRequest(String meUserId, Long requestId) {
        Member me = getMemberByUserId(meUserId);

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다. id=" + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        if (!request.getToMember().getId().equals(me.getId())) {
            throw new AuthError("forbidden", "해당 친구 요청을 거절할 권한이 없습니다.");
        }

        request.reject();
    }

    /**
     * 내 친구 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendSummaryDTO> getMyFriends(String meUserId) {
        Member me = getMemberByUserId(meUserId);
        List<Friend> friends = friendRepository.findAllByMember(me);

        return friends.stream()
                .map(f -> {
                    Member other = f.otherSide(me);
                    return new FriendSummaryDTO(
                            other.getId(),
                            other.getNickname(),   // Member에 맞게 필드명 수정 필요
                            other.getEmail(),      // 없으면 null 리턴되게
                            other.getProfile()     // 프로필 이미지 필드명에 맞게 조정
                    );
                })
                .toList();
    }

    /**
     * 친구 삭제 (양쪽 관계 모두 해제됨 == Friend row 삭제)
     */
    @Transactional
    public void removeFriend(String meUserId, Long friendMemberId) {
        Member me = getMemberByUserId(meUserId);
        Member other = getMemberById(friendMemberId);

        friendRepository.findBetween(me, other)
                .ifPresent(friendRepository::delete);
    }

    /**
     * 차단 추가
     * - 친구였다면 친구 관계도 삭제
     */
    @Transactional
    public void block(String meUserId, Long targetMemberId) {
        Member me = getMemberByUserId(meUserId);
        Member target = getMemberById(targetMemberId);

        if (blockRepository.existsByBlockerAndBlocked(me, target)) {
            return; // 이미 차단 상태면 그냥 무시
        }

        // 친구 관계 삭제
        friendRepository.findBetween(me, target)
                .ifPresent(friendRepository::delete);

        Block block = new Block(me, target);
        blockRepository.save(block);
    }

    /**
     * 차단 해제
     */
    @Transactional
    public void unblock(String meUserId, Long targetMemberId) {
        Member me = getMemberByUserId(meUserId);
        Member target = getMemberById(targetMemberId);

        blockRepository.findByBlockerAndBlocked(me, target)
                .ifPresent(blockRepository::delete);
    }

    /**
     * 내 차단 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BlockResponseDTO> getMyBlocks(String meUserId) {
        Member me = getMemberByUserId(meUserId);

        return blockRepository.findByBlocker(me).stream()
                .map(b -> {
                    Member blocked = b.getBlocked();
                    return new BlockResponseDTO(
                            blocked.getId(),
                            blocked.getNickname(),
                            blocked.getEmail()
                    );
                })
                .toList();
    }

    /**
     * 내가 받은 대기중(PENDING) 친구 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendRequestResponseDTO> getIncomingPendingRequests(String meUserId) {
        Member me = getMemberByUserId(meUserId);

        return friendRequestRepository
                .findByToMemberAndStatus(me, FriendRequestStatus.PENDING)
                .stream()
                .map(fr -> new FriendRequestResponseDTO(
                        fr.getId(),
                        fr.getFromMember().getId(),
                        fr.getToMember().getId(),
                        fr.getMessage(),
                        fr.getStatus()
                ))
                .toList();
    }

    /**
     * 내가 보낸 대기중(PENDING) 친구 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FriendRequestResponseDTO> getOutgoingPendingRequests(String meUserId) {
        Member me = getMemberByUserId(meUserId);

        return friendRequestRepository
                .findByFromMemberAndStatus(me, FriendRequestStatus.PENDING)
                .stream()
                .map(fr -> new FriendRequestResponseDTO(
                        fr.getId(),
                        fr.getFromMember().getId(),
                        fr.getToMember().getId(),
                        fr.getMessage(),
                        fr.getStatus()
                ))
                .toList();
    }
}
