package com.backend.roomMember.repository;


import com.backend.member.domain.Member;
import com.backend.room.domain.Room;
import com.backend.roomMember.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember,Long> {
    boolean existsByRoomAndMember(Room room, Member member);
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);

    @Query("select rm from RoomMember rm join fetch rm.member where rm.room = :room") //N+1 문제 방지를 위해 fetch 조인 커스텀
    List<RoomMember> findAllByRoom(@Param("room") Room room);

    @Query("select rm from RoomMember rm join fetch rm.room where rm.member = :member")
    List<RoomMember> findAllByMember(@Param("member") Member member);
}
