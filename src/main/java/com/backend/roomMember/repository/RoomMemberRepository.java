package com.backend.roomMember.repository;


import com.backend.roomMember.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember,Long> {
}
