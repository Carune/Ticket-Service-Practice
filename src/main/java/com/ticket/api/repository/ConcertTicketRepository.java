package com.ticket.api.repository;

import com.ticket.api.entity.ConcertTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertTicketRepository extends JpaRepository<ConcertTicket, Long> {
}