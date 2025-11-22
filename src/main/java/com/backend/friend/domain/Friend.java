package com.backend.friend.domain;

import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_friends",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friend_from_to",
                        columnNames = {"from_member_id", "to_member_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 나(팔로우 하는 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    // 상대(팔로우 당하는 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Friend(Member fromMember, Member toMember) {
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.createdAt = LocalDateTime.now();
    }

    public static Friend of(Member from, Member to) {
        return new Friend(from, to);
    }
}
