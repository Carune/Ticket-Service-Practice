package com.ticket.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WAITING_KEY = "waiting_queue";
    private static final String ACTIVE_KEY_PREFIX = "active:user:";

    /*
     * 대기열 등록 (줄 서기)
     * - Redis Sorted Set을 사용 (Score: 시간)
     * - FIFO
     */
    public void addQueue(String userId) {
        long now = System.currentTimeMillis();

        // ZADD waiting_queue {now} {userId}
        redisTemplate.opsForZSet().add(WAITING_KEY, userId, now);

        log.info("대기열 등록 완료 - User: {}, Time: {}", userId, now);
    }

    /*
     * 내 대기 순번 조회
     * - 0부터 시작하므로 +1
     * - return: 내 앞에 남은 인원 수 (또는 현재 나의 순위)
     */
    public Long getRank(String userId) {
        // 이미 입장 가능한 상태인지 먼저 확인!
        if (isAllowed(userId)) {
            return 0L; // 0이면 "바로 입장하세요"라는 약속
        }

        // 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, userId);

        if (rank == null) {
            return -1L;
        }

        return rank + 1;
    }

    // n명의 유저를 대기열에서 꺼내서 활성화 시킴
    public void allowUser(long count) {
        // ZSET에서 점수(시간)가 가장 낮은 순서대로 count만큼 꺼냄 (Pop)
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().popMin(WAITING_KEY, count);

        if (tuples == null || tuples.isEmpty()) {
            return; // 대기자가 없음
        }

        // 꺼낸 유저들을 '활성화 상태'로 변경 (TTL 5분 설정)
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();

            // active:user:1 키를 생성하고, 5분 뒤에 저절로 사라지게 함
            redisTemplate.opsForValue().set(
                    ACTIVE_KEY_PREFIX + userId,
                    "true",
                    Duration.ofMinutes(5)
            );

            log.info("유저 입장 허용 - User: {}, 유효시간 5분", userId);
        }
    }

    // 이 유저가 활성화된 상태인지(입장했는지) 확인
    public boolean isAllowed(String userId) {
        // Redis에 active:user:{userId} 키가 있는지 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACTIVE_KEY_PREFIX + userId));
    }
}