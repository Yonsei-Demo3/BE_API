package com.backend.roomMember.domain;

import com.backend.member.domain.Member;
import com.backend.room.domain.Room;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "room_id", nullable = false )
    private Room room;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    //TODO: BaseEntity 구현 후 추가, User 역할 구분(방장 or 참가자)

    @Builder
    private RoomMember(Room room, Member member) {
        this.room = room;
        this.member = member;
    }

    public static RoomMember of(Room room, Member member) {
        return new RoomMember(room, member);
    }
}
