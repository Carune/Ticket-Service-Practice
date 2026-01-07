package com.ticket.api.dto;

import com.ticket.api.entity.ConcertSeat;
import com.ticket.api.entity.SeatGrade;
import lombok.Getter;

@Getter
public class ConcertSeatResponse {
    private final Long id;          // 좌석 ID
    private final Integer seatNumber; // 좌석 번호
    private final SeatGrade grade;    // 등급 (VIP, R, S)
    private final Integer price;      // 가격
    private final String status;      // 상태

    public ConcertSeatResponse(ConcertSeat seat) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.grade = seat.getSeatGrade();
        this.price = seat.getPrice();
        this.status = seat.getStatus().name();
    }
}