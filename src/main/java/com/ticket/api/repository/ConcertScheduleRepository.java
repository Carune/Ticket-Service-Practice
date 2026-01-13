package com.ticket.api.repository;

import com.ticket.api.entity.ConcertSchedule;
import com.ticket.api.entity.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {
    List<ConcertSchedule> findByConcertId(Long ConcertId);
}