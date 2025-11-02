package com.backend.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.member.domain.Member;
import com.backend.member.domain.SocialProvider;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /** 이메일 기반 조회 (LOCAL/소셜 공통) */
    Optional<Member> findByEmail(String email);

    /** userId(UUID) 기반 조회 — JWT, 외부 시스템 식별용 */
    Optional<Member> findByUserId(String userId);

    /** 소셜 로그인용 복합 키 조회 */
    Optional<Member> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    /** 존재 여부 검사 */
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsBySocialProviderAndSocialId(SocialProvider provider, String socialId);
}
