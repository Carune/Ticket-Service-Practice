package com.ticket.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertTicket extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 관리용 ID

    @Column(nullable = false, unique = true)
    private String ticketUuid; // 사용자에게 보여줄 예매 번호 (난수)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private ConcertSeat seat;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Builder
    public ConcertTicket(Member member, ConcertSeat seat) {
        this.ticketUuid = UUID.randomUUID().toString(); // 생성 시 자동 부여
        this.member = member;
        this.seat = seat;
        this.bookingDate = LocalDateTime.now();
    }
}