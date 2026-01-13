package com.ticket.api.dto;
import com.ticket.api.entity.ConcertSchedule;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ConcertScheduleResponse {
    private final Long id;
    private final LocalDateTime concertDate;

    public ConcertScheduleResponse(ConcertSchedule schedule) {
        this.id = schedule.getId();
        this.concertDate = schedule.getConcertDate();
    }
}