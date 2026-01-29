package com.ticket.api;

import com.ticket.api.entity.Concert;
import com.ticket.api.entity.ConcertSchedule;
import com.ticket.api.entity.ConcertSeat;
import com.ticket.api.entity.SeatGrade;
import com.ticket.api.repository.ConcertRepository;
import com.ticket.api.repository.ConcertScheduleRepository;
import com.ticket.api.repository.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final ConcertSeatRepository concertSeatRepository;

    @Override
    public void run(String... args) throws Exception {
        // 데이터가 하나도 없을 때만 실행 (중복 생성 방지)
        if (concertRepository.count() > 0) return;

        System.out.println("========== [DataInit] 초기 데이터 생성을 시작합니다 ==========");

        // 1. 공연 생성
        Concert concert = Concert.builder()
                .title("테스트 콘서트")
                .description("테스트용")
                .venue("고척돔")
                .runningTime(180)
                .build();
        concertRepository.save(concert);

        // 2. 스케줄 생성
        ConcertSchedule schedule = ConcertSchedule.builder()
                .concert(concert)
                .concertDate(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(schedule);

        // 3. 좌석 대량 생성 (1번~50번)
        List<ConcertSeat> seats = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            SeatGrade grade;
            int price;

            if (i <= 10) { // 1~10번: VIP석
                grade = SeatGrade.VIP;
                price = 150000;
            } else if (i <= 30) { // 11~30번: R석
                grade = SeatGrade.R;
                price = 120000;
            } else { // 31~50번: S석
                grade = SeatGrade.S;
                price = 90000;
            }

            seats.add(ConcertSeat.builder()
                    .concertSchedule(schedule)
                    .seatNumber(i)
                    .seatGrade(grade)
                    .price(price)
                    .build());
        }

        concertSeatRepository.saveAll(seats); // 한방에 저장 (Bulk Insert)

        System.out.println("========== [DataInit] 콘서트 1개, 스케줄 1개, 좌석 50개 생성 완료! ==========");
    }
}