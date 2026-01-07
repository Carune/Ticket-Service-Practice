package com.ticket.api.dto;

import com.ticket.api.entity.ConcertTicket;
import com.ticket.api.entity.SeatGrade;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TicketResponse {
    private final Long ticketId;
    private final String ticketUuid; // 사용자에게 보여줄 예매 번호
    private final Integer seatNumber;
    private final SeatGrade grade;
    private final Integer price;
    private final String memberName;
    private final LocalDateTime bookingDate;

    public TicketResponse(ConcertTicket ticket) {
        this.ticketId = ticket.getId();
        this.ticketUuid = ticket.getTicketUuid();
        this.seatNumber = ticket.getSeat().getSeatNumber();
        this.grade = ticket.getSeat().getSeatGrade();
        this.price = ticket.getSeat().getPrice();
        this.memberName = ticket.getMember().getName();
        this.bookingDate = ticket.getBookingDate();
    }
}