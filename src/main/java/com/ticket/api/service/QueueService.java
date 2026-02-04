package com.ticket.api.service;

import com.ticket.api.exception.TooManyRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
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
        // 이미 입장해 있는 상태인지 확인 (Active Queue)
        Boolean isActive = redisTemplate.opsForSet().isMember("active:user:", userId);
        if (Boolean.TRUE.equals(isActive)) {
            throw new IllegalStateException("이미 입장 처리된 유저입니다.");
        }

        // 이미 대기열에 있는지 확인 (Waiting Queue)
        Double score = redisTemplate.opsForZSet().score("waiting_queue", userId);
        if (score != null) {
            throw new IllegalStateException("이미 대기열에 등록되어 있습니다.");
        }

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
        // "throttle:rank:유저ID" 키를 3초 동안만 저장
        String throttleKey = "throttle:rank:" + userId;
        Boolean isPass = redisTemplate.opsForValue()
                .setIfAbsent(throttleKey, "check", Duration.ofSeconds(3));

        if (Boolean.FALSE.equals(isPass)) {
            throw new TooManyRequestException("잠시 후 다시 시도해주세요. (3초 대기)");
        }
        // 이미 입장 가능한 상태인지 먼저 확인
        if (isAllowed(userId)) {
            return 0L; // 0이면 바로 입장 약속
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

        // redis pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String userId = tuple.getValue();
                String key = ACTIVE_KEY_PREFIX + userId;

                // (key, seconds, value)
                connection.stringCommands().setEx(
                        key.getBytes(),
                        300, // 5분 (300초)
                        "true".getBytes()
                );
            }
            return null;
        });

        log.info("🚀 유저 {}명 입장 처리 완료 (Pipeline 적용)", tuples.size());
    }

    // 이 유저가 활성화된 상태인지(입장했는지) 확인
    public boolean isAllowed(String userId) {
        // Redis에 active:user:{userId} 키가 있는지 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACTIVE_KEY_PREFIX + userId));
    }

    // 대기열 제거
    public void removeQueue(String userId) {
        redisTemplate.opsForZSet().remove("waiting_queue", userId);
    }
}