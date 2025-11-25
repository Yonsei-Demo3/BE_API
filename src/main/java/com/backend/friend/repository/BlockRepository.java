package com.backend.friend.repository;

import com.backend.friend.domain.Block;
import com.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerAndBlocked(Member blocker, Member blocked);

    Optional<Block> findByBlockerAndBlocked(Member blocker, Member blocked);

    List<Block> findByBlocker(Member blocker);
}
