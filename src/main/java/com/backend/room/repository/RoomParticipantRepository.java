package com.backend.room.repository;

import com.backend.roomMember.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomParticipantRepository extends JpaRepository<RoomMember, Long> {
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);
}
