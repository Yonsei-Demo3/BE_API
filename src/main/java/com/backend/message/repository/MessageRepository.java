package com.backend.message.repository;

import com.backend.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRoomId(Long roomId, Pageable pageable);
    List<Message> findByRoomId(Long roomId);;
}
