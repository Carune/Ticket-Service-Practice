package com.ticket.api.service;

import com.ticket.api.dto.*;

import java.util.List;

public interface ConcertService {
    List<ConcertSeatResponse> getAvailableSeats(Long scheduleId);
    TicketResponse reserveSeat(ReservationRequest request, String email);
    List<ConcertResponse> getAllConcerts();
    List<ConcertScheduleResponse> getConcertSchedules(Long concertId);
}