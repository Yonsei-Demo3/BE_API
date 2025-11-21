package com.backend.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagSearchRepository extends JpaRepository<Tag, Long> {

    // 검색어가 들어간 태그 상위 10개
    List<Tag> findTop10ByNameContainingIgnoreCase(String name);
}
