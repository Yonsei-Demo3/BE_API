package com.backend.roomMember.repository;


import com.backend.member.domain.Member;
import com.backend.room.domain.Room;
import com.backend.roomMember.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember,Long> {
    boolean existsByRoomAndMember(Room room, Member member);
}
