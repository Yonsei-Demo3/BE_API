package com.backend.friend.service;

import com.backend.friend.domain.FriendRequest;
import com.backend.friend.domain.FriendRequestStatus;
import com.backend.friend.dto.FriendRequestCreateDTO;
import com.backend.friend.dto.FriendRequestDTO;
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
@Transactional
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;

    // 친구 요청 보내기
    public FriendRequestDTO sendRequest(String requesterUserId, FriendRequestCreateDTO req) {
        Member requester = findMemberByUserIdOrThrow(requesterUserId);
        Member receiver = findMemberByUserIdOrThrow(req.targetUserId());

        if (requester.getUserId().equals(receiver.getUserId())) {
            throw new AuthError("friend_self", "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 PENDING 상태의 요청이 있으면 막기
        boolean existsPending = friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                requester, receiver, FriendRequestStatus.PENDING
        );
        if (existsPending) {
            throw new AuthError("friend_request_duplicated", "이미 대기중인 친구 요청이 있습니다.");
        }

        FriendRequest entity = FriendRequest.create(requester, receiver, req.message());
        FriendRequest saved = friendRequestRepository.save(entity);

        return toDTO(saved);
    }

    // 내가 받은 요청 (PENDING만)
    @Transactional(readOnly = true)
    public List<FriendRequestDTO> getIncomingRequests(String currentUserId) {
        Member me = findMemberByUserIdOrThrow(currentUserId);
        return friendRequestRepository
                .findByReceiverAndStatusOrderByIdDesc(me, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // 내가 보낸 요청 (전체)
    @Transactional(readOnly = true)
    public List<FriendRequestDTO> getOutgoingRequests(String currentUserId) {
        Member me = findMemberByUserIdOrThrow(currentUserId);
        return friendRequestRepository
                .findByRequesterOrderByIdDesc(me)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // 수락
    public FriendRequestDTO accept(String currentUserId, Long requestId) {
        Member me = findMemberByUserIdOrThrow(currentUserId);

        FriendRequest request = friendRequestRepository.findByIdAndReceiver(requestId, me)
                .orElseThrow(() -> new AuthError("friend_request_not_found", "해당 친구 요청이 없습니다."));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new AuthError("friend_request_invalid_status", "이미 처리된 친구 요청입니다.");
        }

        request.accept();
        // TODO: 필요하다면 여기서 follow 양방향 생성 같은 추가 로직 연결

        return toDTO(request);
    }

    // 거절
    public FriendRequestDTO reject(String currentUserId, Long requestId) {
        Member me = findMemberByUserIdOrThrow(currentUserId);

        FriendRequest request = friendRequestRepository.findByIdAndReceiver(requestId, me)
                .orElseThrow(() -> new AuthError("friend_request_not_found", "해당 친구 요청이 없습니다."));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new AuthError("friend_request_invalid_status", "이미 처리된 친구 요청입니다.");
        }

        request.reject();
        return toDTO(request);
    }

    // ---------- 헬퍼 ----------

    private Member findMemberByUserIdOrThrow(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthError("not_found_member", "존재하지 않는 회원입니다."));
    }

    private FriendRequestDTO toDTO(FriendRequest fr) {
        return new FriendRequestDTO(
                fr.getId(),
                fr.getRequester().getUserId(),
                fr.getRequester().getNickname(),
                fr.getReceiver().getUserId(),
                fr.getReceiver().getNickname(),
                fr.getMessage(),
                fr.getStatus(),
                fr.getCreatedAt(),
                fr.getRespondedAt()
        );
    }
}
