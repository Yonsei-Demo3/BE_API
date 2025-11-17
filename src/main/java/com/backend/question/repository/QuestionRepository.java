package com.backend.question.repository;

import com.backend.question.domain.Question;
import com.backend.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
    public Long findRoomIdById(Long questionId);
    public Optional<Room> findRoomById(Long roomId);
}
