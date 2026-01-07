package com.ticket.api.controller;

import com.ticket.api.dto.ConcertSeatResponse;
import com.ticket.api.dto.ReservationRequest;
import com.ticket.api.dto.TicketResponse;
import com.ticket.api.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
public class ConcertController {

    private final ConcertService concertService;

    /*
    API: 특정 스케줄의 예약 가능 좌석 조회
    GET /api/v1/concerts/{scheduleId}/seats
    */
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<List<ConcertSeatResponse>> getAvailableSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(concertService.getAvailableSeats(scheduleId));
    }

    // 예매 API
    @PostMapping("/reserve")
    public ResponseEntity<TicketResponse> reserveSeat(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(concertService.reserveSeat(request));
    }
}