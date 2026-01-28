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
    private RedisTemplate<String, Object> redisTemplate;

    // 테스트 전에 Redis clear
    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("1000명이 한꺼번에 줄을 서면 정확히 순서대로 들어갈까?")
    void massive_queue_test() throws InterruptedException {
        int peopleCount = 1000;

        long start = System.currentTimeMillis();

        // 1. 1000명 줄 세우기 (반복문)
        for (int i = 0; i < peopleCount; i++) {
            String userId = "user_" + i;
            queueService.addQueue(userId);
        }

        long end = System.currentTimeMillis();
        System.out.println("====== 1000명 줄 서기 완료: " + (end - start) + "ms ======");

        // 2. 검증: 1000명이 잘 들어갔나?
        Long rankOfLastUser = queueService.getRank("user_999"); // 마지막 사람
        Long rankOfFirstUser = queueService.getRank("user_0");  // 첫번째 사람

        assertThat(rankOfFirstUser).isEqualTo(1L); // 1등
        assertThat(rankOfLastUser).isEqualTo(1000L); // 1000등

        System.out.println("====== 검증 완료: 순서가 정확하게 보장됨 ======");
    }
}