package com.ticket.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationRequest {
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;

    @NotNull(message = "이메일은 필수입니다.")
    private String email; // 실제론 토큰에서 꺼내야 하지만, 테스트용으로 입력받음
}