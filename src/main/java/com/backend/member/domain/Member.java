package com.backend.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
    name = "members",
    indexes = {
        @Index(name = "idx_member_email_unique", columnList = "email", unique = true),
        @Index(name = "idx_member_user_id_unique", columnList = "user_id", unique = true),
        @Index(name = "idx_member_social", columnList = "social_provider,social_id")
    },
    uniqueConstraints = {
        // 소셜 로그인 고유성 보장 (provider + social_id)
        @UniqueConstraint(name = "uk_member_social_provider_id", columnNames = {"social_provider", "social_id"})
    }
)
public class Member {

    // 내부 PK (DB 관계용)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 공개용 안정적 식별자
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    /** 이메일 (LOCAL/소셜 공통, 비즈니스 정책상 필수로 유지) */
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** 비밀번호(BCrypt 60자). 소셜 계정은 더미 해시 저장 가능 */
    @Column(nullable = false, length = 60)
    private String password;

    /** 닉네임 */
    @Column(nullable = false, length = 50)
    private String nickname;

    /** 권한 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /** 소셜 로그인 제공자 (KAKAO 등). LOCAL이면 null */
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", length = 20)
    private SocialProvider socialProvider;

    /** 소셜 식별자 — provider와 함께 유니크 */
    @Column(name = "social_id", length = 100)
    private String socialId;

    /** 프로필 이미지 URL */
    @Column(name = "profile", length = 500)
    private String profile;

    /** 전화번호 */
    @Column(name = "phone", length = 30)
    private String phone;

    /** 생성 시 userId 자동 채번 (없으면 UUID) */
    @PrePersist
    private void ensureUserId() {
        if (this.userId == null || this.userId.isBlank()) {
            this.userId = UUID.randomUUID().toString();
        }
    }

    @Builder
    private Member(
            String userId,
            String email,
            String password,
            String nickname,
            Role role,
            SocialProvider socialProvider,
            String socialId,
            String profile,
            String phone
    ) {
        this.userId = userId; // null이면 @PrePersist에서 UUID 채움
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        if (role != null) this.role = role;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.profile = profile;
        this.phone = phone;
    }

    /** LOCAL 회원 생성 헬퍼 */
    public static Member local(String userId, String email, String encodedPassword, String nickname) {
        return Member.builder()
                .userId(userId)                 // null 전달 시 @PrePersist가 UUID 생성
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.USER)
                .build();
    }

    /** 소셜 회원 생성 헬퍼 (비밀번호는 더미 해시/랜덤) */
    public static Member social(SocialProvider provider, String socialId, String email, String encodedDummyPwd, String nickname) {
        return Member.builder()
                .email(email)
                .password(encodedDummyPwd)
                .nickname(nickname)
                .role(Role.USER)
                .socialProvider(provider)
                .socialId(socialId)
                .build();
    }

    // 변경 메서드
    public void changeNickname(String nickname) { this.nickname = nickname; }
    public void changeRole(Role role) { this.role = role; }
    public void changeProfile(String profile) { this.profile = profile; }
    public void changePhone(String phone) { this.phone = phone; }
}
