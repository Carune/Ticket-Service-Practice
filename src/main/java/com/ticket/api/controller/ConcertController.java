package com.ticket.api.controller;

import com.ticket.api.annotation.QueuePass;
import com.ticket.api.dto.*;
import com.ticket.api.service.ConcertService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    // 공연 목록 조회
    @GetMapping
    public ResponseEntity<List<ConcertResponse>> getAllConcerts(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(concertService.getAllConcerts(pageable));
    }

    // 특정 공연의 스케줄 조회
    @GetMapping("/{concertId}/schedules")
    public ResponseEntity<List<ConcertScheduleResponse>> getConcertSchedules(@PathVariable Long concertId) {
        return ResponseEntity.ok(concertService.getConcertSchedules(concertId));
    }

    @QueuePass
    @PostMapping("/reserve")
    public ResponseEntity<TicketResponse> reserveSeat(
            @Valid @RequestBody ReservationRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(concertService.reserveSeat(request, principal.getName()));
    }
}