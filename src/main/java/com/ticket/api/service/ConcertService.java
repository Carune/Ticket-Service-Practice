package com.ticket.api.service;

import com.ticket.api.dto.ConcertSeatResponse;
import com.ticket.api.dto.ReservationRequest;
import com.ticket.api.dto.TicketResponse;

import java.util.List;

public interface ConcertService {
    List<ConcertSeatResponse> getAvailableSeats(Long scheduleId);
    TicketResponse reserveSeat(ReservationRequest request);
}