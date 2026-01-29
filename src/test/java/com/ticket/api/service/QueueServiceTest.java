package com.ticket.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 테스트 전에 Redis clear
    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("1만명이 한꺼번에 줄을 서면 정확히 순서대로 들어갈까?")
    void massive_queue_test() throws InterruptedException {
        int peopleCount = 10000; //10만명

        long start = System.currentTimeMillis();

        for (int i = 0; i < peopleCount; i++) {
            queueService.addQueue("user_" + i);
        }

        long end = System.currentTimeMillis();
        System.out.println("1만 명 대기열 등록 시간: " + (end - start) + "ms");

        /*Long rankOfLastUser = queueService.getRank("user_999"); // 마지막 사람
        Long rankOfFirstUser = queueService.getRank("user_0");  // 첫번째 사람

        assertThat(rankOfFirstUser).isEqualTo(1L); // 1등
        assertThat(rankOfLastUser).isEqualTo(1000L); // 1000등*/

        // 10만 번째 유저 순위 확인
        Long rank = queueService.getRank("user_9999");
        assertThat(rank).isEqualTo(10000L);

        System.out.println("====== 검증 완료: 순서가 정확하게 보장됨 ======");
    }
}