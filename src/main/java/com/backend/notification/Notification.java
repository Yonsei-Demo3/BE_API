package com.backend.notification;

import com.backend.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

//TODO: 알림에 따라 socket을 통해 전달하는 내용이 너무 달라짐... 하위 클래스로 구현 고려
@Entity
@Getter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member receiver;

    @Column(nullable = false)
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private Notification(Member receiver, NotificationType type) {
        this.receiver = receiver;
        this.type = type;
    }

    public static Notification of(Member receiver, NotificationType type) {
        return new Notification(receiver, type);
    }

    public void read() {
        this.isRead = true;
    }
}
