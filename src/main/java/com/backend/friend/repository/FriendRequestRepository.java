package com.backend.friend.repository;

import com.backend.friend.domain.FriendRequest;
import com.backend.friend.domain.FriendRequestStatus;
import com.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // 이미 보낸 대기중 요청 있는지 체크
    boolean existsByRequesterAndReceiverAndStatus(
            Member requester,
            Member receiver,
            FriendRequestStatus status
    );

    // 내가 보낸 요청 리스트
    List<FriendRequest> findByRequesterOrderByIdDesc(Member requester);

    // 내가 받은 요청 리스트 (주로 PENDING 용)
    List<FriendRequest> findByReceiverAndStatusOrderByIdDesc(
            Member receiver,
            FriendRequestStatus status
    );

    // 특정 요청 + 수신자 검증용
    Optional<FriendRequest> findByIdAndReceiver(Long id, Member receiver);
}
