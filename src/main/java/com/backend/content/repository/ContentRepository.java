package com.backend.content.repository;

import com.backend.content.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByName(String name);
}
