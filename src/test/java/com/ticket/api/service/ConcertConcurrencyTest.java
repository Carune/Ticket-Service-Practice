package com.ticket.api.service;

import com.ticket.api.dto.ReservationRequest;
import com.ticket.api.entity.Concert;
import com.ticket.api.entity.ConcertSchedule;
import com.ticket.api.entity.ConcertSeat;
import com.ticket.api.entity.Member;
import com.ticket.api.entity.SeatGrade;
import com.ticket.api.repository.ConcertRepository;
import com.ticket.api.repository.ConcertScheduleRepository;
import com.ticket.api.repository.ConcertSeatRepository;
import com.ticket.api.repository.ConcertTicketRepository;
import com.ticket.api.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcertConcurrencyTest {

    @Autowired
    private ConcertService concertService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ConcertSeatRepository concertSeatRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;
    @Autowired
    private ConcertTicketRepository concertTicketRepository;

    @Test
    @DisplayName("좌석_예약_동시성_성공_테스트")
    void concurrency_test_optimistic_lock() throws InterruptedException {
        // 1. [준비] 테스트용 데이터 생성 (콘서트, 스케줄, 좌석 1개, 유저 100명)

        // 공연 생성
        Concert concert = concertRepository.save(Concert.builder()
                .title("테스트 콘서트")
                .description("동시성 테스트")
                .venue("테스트홀")
                .runningTime(100)
                .build());

        // 스케줄 생성
        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder()
                .concert(concert)
                .concertDate(LocalDateTime.now().plusDays(10))
                .build());

        // 경쟁할 좌석 1개 생성 (ID를 모르면 안되니까 저장 후 객체 보관)
        ConcertSeat targetSeat = concertSeatRepository.save(ConcertSeat.builder()
                .concertSchedule(schedule)
                .seatNumber(1)
                .price(100000)
                .seatGrade(SeatGrade.VIP)
                .build());
        Long seatId = targetSeat.getId();

        // 유저 100명 미리 가입 (user0 ~ user99)
        int threadCount = 100;
        for (int i = 0; i < threadCount; i++) {
            memberRepository.save(Member.builder()
                    .email("user" + i + "@test.com")
                    .password("1234")
                    .name("User" + i)
                    .build());
        }

        // 2. [실행] 멀티 스레드 세팅
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개 스레드 풀
        CountDownLatch latch = new CountDownLatch(threadCount); // 100명이 다 끝날 때까지 기다리는 장치

        AtomicInteger successCount = new AtomicInteger(0); // 성공 횟수 카운터
        AtomicInteger failCount = new AtomicInteger(0);    // 실패 횟수 카운터

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    // 요청 객체 생성 (user0, user1 ... user99 가 각각 요청)
                    ReservationRequest request = new ReservationRequest();
                    ReflectionTestUtils.setField(request, "seatId", seatId);
                    ReflectionTestUtils.setField(request, "email", "user" + idx + "@test.com");

                    // 서비스 호출 (예매 시도!)
                    concertService.reserveSeat(request, "user" + idx + "@test.com");

                    // 예외가 안 나면 성공
                    successCount.incrementAndGet();
                    System.out.println("성공! User" + idx);

                } catch (Exception e) {
                    // 이미 예약됨 or 낙관적 락 충돌 -> 실패로 간주
                    failCount.incrementAndGet();
                    System.out.println("실패: " + e.getMessage());
                } finally {
                    latch.countDown(); // 완료 신호 보냄
                }
            });
        }

        latch.await(); // 100명이 다 끝날 때까지 여기서 대기
        long endTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("총 소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("=========================================");

        // 3. [검증]
        // 성공은 무조건 1명이어야 함.
        assertThat(successCount.get()).isEqualTo(1);

        // 나머지는 다 실패해야 함.
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        // 실제 DB 상태 확인: 좌석은 RESERVED 여야 함
        ConcertSeat seatAfter = concertSeatRepository.findById(seatId).orElseThrow();
        assertThat(seatAfter.getStatus()).isEqualTo(ConcertSeat.SeatStatus.RESERVED);

        // 실제 티켓도 딱 1장만 생겨야 함
        long ticketCount = concertTicketRepository.count();
        // (기존 데이터가 있을 수 있으니, 이번 테스트에서 생성된 것만 확인하려면 좀 복잡하지만,
        //  DataInit 제외하고 생각하면 1장이어야 함. 일단 로그로 확인 권장)
    }
}