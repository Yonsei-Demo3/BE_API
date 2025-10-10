package com.backend.member.domain;

import jakarta.persistence.*;
// 이메일 형식 검증용
import jakarta.validation.constraints.Email;

/**
 * Member 엔티티
 * - 회원 정보를 저장하는 도메인 객체
 * - BaseTimeEntity를 상속하여 생성/수정 시각을 자동 관리함
 */
@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_email_unique", columnList = "email", unique = true)
})
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;  // 기본값 USER

    protected Member() {} // JPA 기본 생성자

    private Member(String email, String rawEncodedPassword, String nickname, Role role) {
        this.email = email;
        this.password = rawEncodedPassword;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member of(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname, Role.USER);
    }


    // ------------- buisness logic -------------
    // nickname 변경
    public void changeNickname(String nickname) { this.nickname = nickname; }
    public void changeRole(Role role) { this.role = role; }

    // getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public Role getRole() { return role; }
}