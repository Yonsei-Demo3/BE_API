package com.backend.friend.repository;

import com.backend.friend.domain.FriendRequest;
import com.backend.friend.domain.FriendRequestStatus;
import com.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByToMemberAndStatus(Member toMember, FriendRequestStatus status);

    List<FriendRequest> findByFromMemberAndStatus(Member fromMember, FriendRequestStatus status);

    Optional<FriendRequest> findByFromMemberAndToMemberAndStatus(
            Member fromMember,
            Member toMember,
            FriendRequestStatus status
    );
}
