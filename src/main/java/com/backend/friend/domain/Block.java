package com.backend.friend.domain;

import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_blocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_block_pair",
                        columnNames = {"blocker_id", "blocked_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단을 건 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blocker;

    // 차단 당한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blocked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Block(Member blocker, Member blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.createdAt = LocalDateTime.now();
    }
}
