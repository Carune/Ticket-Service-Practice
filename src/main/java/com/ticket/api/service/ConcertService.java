package com.ticket.api.service;

import com.ticket.api.dto.*;
import com.ticket.api.entity.*;
import com.ticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 전용
public class ConcertService {

    private final ConcertSeatRepository concertSeatRepository;
    private final ConcertTicketRepository concertTicketRepository;
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final MemberRepository memberRepository;

    public List<ConcertSeatResponse> getAvailableSeats(Long scheduleId) {
        // 해당 스케줄의 AVAILABLE 좌석만 DB에서 가져옴
        List<ConcertSeat> seats = concertSeatRepository.findByConcertScheduleIdAndStatus(
                scheduleId,
                ConcertSeat.SeatStatus.AVAILABLE
        );

        // Entity 리스트를 DTO 리스트로 변환해서 반환
        return seats.stream()
                .map(ConcertSeatResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse reserveSeat(ReservationRequest request, String email) {
        // 좌석 조회 (없으면 에러)
        ConcertSeat seat = concertSeatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 이미 예약된 좌석인지 확인
        if (seat.getStatus() != ConcertSeat.SeatStatus.AVAILABLE) {
            throw new IllegalArgumentException("이미 예약된 좌석입니다.");
        }

        // 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 좌석 상태 변경 (AVAILABLE -> RESERVED)
        // 여기서 JPA가 DB의 version과 비교해서, 다르면 낙관적 락 exception
        seat.reserve();

        // 5. 티켓 생성 및 저장
        ConcertTicket ticket = ConcertTicket.builder()
                .member(member)
                .seat(seat)
                .build();

        return new TicketResponse(concertTicketRepository.save(ticket));
    }

    public List<ConcertResponse> getAllConcerts(Pageable pageable) {
        Page<Concert> concertPage = concertRepository.findAll(pageable);
        return concertPage.getContent().stream().map(ConcertResponse::new).collect(Collectors.toList());
    }

    public List<ConcertScheduleResponse> getConcertSchedules(Long concertId){
        List<ConcertSchedule> scheduleList = concertScheduleRepository.findByConcertId(concertId);
        return scheduleList.stream().map(ConcertScheduleResponse::new).collect(Collectors.toList());
    }
}