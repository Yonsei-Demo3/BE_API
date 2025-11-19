package com.backend.question.repository;

import com.backend.question.domain.Question;
import com.backend.room.domain.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
    Long findRoomIdById(Long questionId);
    Optional<Room> findRoomById(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Question q where q.id = :id")
    Optional<Question> findByIdWithLock(@Param("id")Long id);


}
