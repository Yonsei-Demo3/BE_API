package com.backend.friend.repository;

import com.backend.friend.domain.Friend;
import com.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.member1 = :member OR f.member2 = :member)
        """)
    List<Friend> findAllByMember(@Param("member") Member member);

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.member1 = :a AND f.member2 = :b)
           OR (f.member1 = :b AND f.member2 = :a)
        """)
    Optional<Friend> findBetween(@Param("a") Member a, @Param("b") Member b);

    @Query("""
        SELECT COUNT(f) > 0 FROM Friend f
        WHERE (f.member1 = :a AND f.member2 = :b)
           OR (f.member1 = :b AND f.member2 = :a)
        """)
    boolean existsBetween(@Param("a") Member a, @Param("b") Member b);
}
