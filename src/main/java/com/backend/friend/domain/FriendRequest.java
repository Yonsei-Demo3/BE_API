package com.backend.friend.domain;

import com.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요청 보낸 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    // 요청 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // ---------- 생성 & 상태 변경 ----------

    private FriendRequest(Member requester, Member receiver, String message) {
        this.requester = requester;
        this.receiver = receiver;
        this.message = message;
        this.status = FriendRequestStatus.PENDING;
    }

    public static FriendRequest create(Member requester, Member receiver, String message) {
        return new FriendRequest(requester, receiver, message);
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }
}
