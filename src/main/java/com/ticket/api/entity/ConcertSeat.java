package com.ticket.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 스케줄ID + 좌석번호는 유니크해야 함 (한 회차에 1번 좌석이 두 개일 수 없음)
@Table(
        indexes = @Index(name = "idx_schedule_seat", columnList = "concert_schedule_id, seatNumber"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"concert_schedule_id", "seatNumber"})
)
public class ConcertSeat extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id", nullable = false)
    private ConcertSchedule concertSchedule;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatGrade seatGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Version
    private Long version;

    @Builder
    public ConcertSeat(ConcertSchedule concertSchedule, Integer seatNumber, Integer price, SeatGrade seatGrade) {
        this.concertSchedule = concertSchedule;
        this.seatNumber = seatNumber;
        this.price = price;
        this.seatGrade = seatGrade;
    }

    public enum SeatStatus {
        AVAILABLE, RESERVED, SOLD
    }

    // 상태 변경 메서드 (Setter 대신 사용)
    public void reserve() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("예약 불가능한 상태입니다.");
        }
        this.status = SeatStatus.RESERVED;
    }
}