package com.ticket.api.controller;

import com.ticket.api.annotation.QueuePass;
import com.ticket.api.dto.*;
import com.ticket.api.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "콘서트 예약 API", description = "특정 스케줄 예약 가능 좌석 조회, 콘서트 목록/좌석 조회, 좌석 예약")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
public class ConcertController {

    private final ConcertService concertService;

    /*
    API: 특정 스케줄의 예약 가능 좌석 조회
    GET /api/v1/concerts/{scheduleId}/seats
    */
    @Operation(summary = "예약 가능 좌석 조회", description = "특정 스케줄의 예약 가능한 좌석 리스트를 조회합니다.")
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<List<ConcertSeatResponse>> getAvailableSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(concertService.getAvailableSeats(scheduleId));
    }

    // 공연 목록 조회
    @Operation(summary = "콘서트 목록 조회", description = "전체 콘서트 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ConcertResponse>> getAllConcerts(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(concertService.getAllConcerts(pageable));
    }

    // 특정 공연의 스케줄 조회
    @Operation(summary = "콘서트 스케줄 조회", description = "특정 콘서트의 날짜별 스케줄을 조회합니다.")
    @GetMapping("/{concertId}/schedules")
    public ResponseEntity<List<ConcertScheduleResponse>> getConcertSchedules(@PathVariable Long concertId) {
        return ResponseEntity.ok(concertService.getConcertSchedules(concertId));
    }

    @Operation(summary = "좌석 예약 요청", description = "대기열을 통과한 유저만 예약이 가능합니다.")
    @QueuePass
    @PostMapping("/reserve")
    public ResponseEntity<TicketResponse> reserveSeat(
            @Valid @RequestBody ReservationRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(concertService.reserveSeat(request, principal.getName()));
    }
}