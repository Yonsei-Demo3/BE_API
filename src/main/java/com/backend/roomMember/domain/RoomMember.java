package com.backend.roomMember.domain;

import com.backend.member.domain.Member;
import com.backend.room.domain.Room;
import com.backend.roomMember.util.RandomNicknameGenerator;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "room_members", // 1. DB 테이블 이름 명시
        uniqueConstraints = {
                // 2. 한 유저가 한 방에 두 번 들어가는 것을 DB에서 원천 차단
                @UniqueConstraint(
                        name = "room_member_uk",
                        columnNames = {"room_id", "member_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false )
    private Room room;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomMemberStatus roomMemberStatus;

    @Column(name = "room_nickname", nullable = false)
    private String roomNickname;

    public void doReady() {
        this.roomMemberStatus = RoomMemberStatus.READY;
    }

    //TODO: BaseEntity 구현 후 추가, User 역할 구분(방장 or 참가자)

    @Builder
    private RoomMember(Room room, Member member, RoomMemberStatus status, String roomNickname) {
        this.room = room;
        this.member = member;
        this.roomMemberStatus = status;
        this.roomNickname = roomNickname;
    }

    //처음엔 무조건 join으로 시작할것임 TODO: 네이밍 고려
    public static RoomMember of(Room room, Member member, Set<String> existingNicknames) {
        String nickname = RandomNicknameGenerator.generate(existingNicknames);

        return RoomMember.builder()
                .room(room)
                .member(member)
                .status(RoomMemberStatus.JOINED)
                .roomNickname(nickname)
                .build();
    }
}
