package com.backend.question.repository;

import com.backend.member.domain.Member;
import com.backend.question.domain.Question;
import com.backend.question.domain.QuestionStatus;
import com.backend.room.domain.Room;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryCustom {
    Long findRoomIdById(Long questionId);
    Optional<Room> findRoomById(Long roomId);

    //현재 참여인원 수정 시 동시성 문제 해결을 위해 비관적락 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Question q where q.id = :id")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Question> findByIdWithLock(@Param("id")Long id);

    List<Question> findByRoomIn(List<Room> rooms);
    List<Question> findByRoomInAndStatus(List<Room> rooms, QuestionStatus status);
    List<Question> findByHost(Member member);

}
