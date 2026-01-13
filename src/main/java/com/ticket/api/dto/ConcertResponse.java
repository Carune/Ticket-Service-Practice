package com.ticket.api.dto;
import com.ticket.api.entity.Concert;
import lombok.Getter;

@Getter
public class ConcertResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String venue;

    public ConcertResponse(Concert concert) {
        this.id = concert.getId();
        this.title = concert.getTitle();
        this.description = concert.getDescription();
        this.venue = concert.getVenue();
    }
}