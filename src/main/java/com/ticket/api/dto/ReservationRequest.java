package com.ticket.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationRequest {
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;
}