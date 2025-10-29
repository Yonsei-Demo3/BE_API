package com.backend.chatroom.repository;

import com.backend.chatroom.domain.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
