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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    @DisplayName("좌석_예약_동시성_성공_테스트")
    void concurrency_test_optimistic_lock() throws InterruptedException {
        // 1. [준비] 테스트용 데이터 생성 (콘서트, 스케줄, 좌석 1개, 유저 1000명)

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

        // 유저 1000명 미리 가입 (user0 ~ user999)
        int threadCount = 1000;
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            members.add(Member.builder()
                    .email("user" + i + "@test.com")
                    .password("1234")
                    .name("User" + i)
                    .build());
        }
        memberRepository.saveAll(members);

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        AtomicInteger optimisticLockFailCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    ReflectionTestUtils.setField(request, "seatId", seatId);

                    concertService.reserveSeat(request, "user" + idx + "@test.com");

                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failCount.incrementAndGet();

                    // 실패 원인이 '낙관적 락'이나 '이미 예약됨'이면 카운트 증가
                    if (e instanceof ObjectOptimisticLockingFailureException
                            || e.getMessage().contains("이미 예약된")) {
                        optimisticLockFailCount.incrementAndGet();
                    }

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("총 소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("낙관적 락(버전충돌) 실패 추정: " + optimisticLockFailCount.get());
        System.out.println("=========================================");

        //  [검증]
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        ConcertSeat seatAfter = concertSeatRepository.findById(seatId).orElseThrow();
        assertThat(seatAfter.getStatus()).isEqualTo(ConcertSeat.SeatStatus.RESERVED);
    }
}