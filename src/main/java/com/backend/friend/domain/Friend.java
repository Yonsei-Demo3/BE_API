package com.backend.friend.domain;

import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friends",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friend_pair",
                        columnNames = {"member1_id", "member2_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 항상 ID가 더 작은 쪽을 member1 로 저장해서 중복 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member1_id", nullable = false)
    private Member member1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member2_id", nullable = false)
    private Member member2;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Friend(Member a, Member b) {
        if (a.getId() < b.getId()) {
            this.member1 = a;
            this.member2 = b;
        } else {
            this.member1 = b;
            this.member2 = a;
        }
        this.createdAt = LocalDateTime.now();
    }

    public boolean involves(Member member) {
        return member1.equals(member) || member2.equals(member);
    }

    public Member otherSide(Member me) {
        if (member1.equals(me)) return member2;
        if (member2.equals(me)) return member1;
        return null;
    }
}
