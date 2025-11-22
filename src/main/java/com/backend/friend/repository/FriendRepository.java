package com.backend.friend.repository;

import com.backend.friend.domain.Friend;
import com.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByFromMemberAndToMember(Member from, Member to);

    void deleteByFromMemberAndToMember(Member from, Member to);

    long countByFromMember(Member from);   // 내가 팔로우하는 수 (following)
    long countByToMember(Member to);       // 나를 팔로우하는 수 (follower)

    List<Friend> findByFromMember(Member from); // 내가 팔로우하는 사람들
    List<Friend> findByToMember(Member to);     // 나를 팔로우하는 사람들
}
