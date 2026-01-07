package com.ticket.api.repository;

import com.ticket.api.entity.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {

    List<ConcertSeat> findByConcertScheduleIdAndStatus(Long concertScheduleId, ConcertSeat.SeatStatus status);
}