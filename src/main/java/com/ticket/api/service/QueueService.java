package com.ticket.api.service;

import com.ticket.api.exception.TooManyRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WAITING_KEY = "waiting_queue";
    private static final String ACTIVE_KEY_PREFIX = "active:user:";
    private static final String RANK_THROTTLE_PREFIX = "throttle:rank:";

    @Value("${queue.active-ttl-seconds:300}")
    private int activeTtlSeconds;

    @Value("${queue.rank-throttle-seconds:3}")
    private int rankThrottleSeconds;

    /*
     * 대기열 등록 (진입)
     * - Redis Sorted Set 사용 (Score: 시간)
     * - FIFO
     * - 상태 정책: WAITING(ZSET) -> ACTIVE(KEY, TTL)
     *   ACTIVE 키가 살아있는 동안은 재진입 불가, 만료 후 재진입 가능
     */
    public void addQueue(String userId) {
        // 이미 입장 가능한 상태인지 확인 (Active Queue)
        Boolean isActive = redisTemplate.hasKey(ACTIVE_KEY_PREFIX + userId);
        if (Boolean.TRUE.equals(isActive)) {
            throw new IllegalStateException("이미 입장 처리된 사용자입니다.");
        }

        // 이미 대기열에 있는지 확인 (Waiting Queue)
        Double score = redisTemplate.opsForZSet().score(WAITING_KEY, userId);
        if (score != null) {
            throw new IllegalStateException("이미 대기열에 등록되어 있습니다.");
        }

        long now = System.currentTimeMillis();

        // ZADD waiting_queue {now} {userId}
        redisTemplate.opsForZSet().add(WAITING_KEY, userId, now);

        log.info("대기열 등록 완료 - User: {}, Time: {}", userId, now);
    }

    /*
     * 대기열 순번 조회
     * - 0부터 시작하므로 +1
     * - return: 앞에 대기 인원 수(=현재 순서)
     */
    public Long getRank(String userId) {
        // "throttle:rank:userID" 키를 일정 시간 동안만 유지
        String throttleKey = RANK_THROTTLE_PREFIX + userId;
        Boolean isPass = redisTemplate.opsForValue()
                .setIfAbsent(throttleKey, "check", Duration.ofSeconds(rankThrottleSeconds));

        if (Boolean.FALSE.equals(isPass)) {
            throw new TooManyRequestException("잠시 후 다시 시도해주세요. (" + rankThrottleSeconds + "초 대기)");
        }
        // 이미 입장 가능한 상태인지 먼저 확인
        if (isAllowed(userId)) {
            return 0L; // 0이면 바로 입장 접속
        }

        // 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, userId);

        if (rank == null) {
            return -1L;
        }

        return rank + 1;
    }

    // n명의 사용자를 대기열에서 꺼내 활성 상태로 전환
    public void allowUser(long count) {
        // ZSET에서 최소 Score 순으로 count만큼 Pop
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().popMin(WAITING_KEY, count);

        if (tuples == null || tuples.isEmpty()) {
            return; // 대기자가 없음
        }

        List<ZSetOperations.TypedTuple<String>> tupleList = new ArrayList<>(tuples);
        int batchSize = 1000;

        for (int i = 0; i < tupleList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, tupleList.size());
            List<ZSetOperations.TypedTuple<String>> batch = tupleList.subList(i, end);

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (ZSetOperations.TypedTuple<String> tuple : batch) {
                    String userId = tuple.getValue();
                    String key = ACTIVE_KEY_PREFIX + userId;
                    connection.stringCommands().setEx(key.getBytes(), activeTtlSeconds, "true".getBytes());
                }
                return null;
            });
        }

        log.info("총 사용자 {}명 입장 처리 완료 (Pipeline 적용)", tuples.size());
    }

    // 사용자가 활성 상태인지(입장 가능한지) 확인
    public boolean isAllowed(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACTIVE_KEY_PREFIX + userId));
    }

    // 대기열 제거
    public void removeQueue(String userId) {
        redisTemplate.opsForZSet().remove(WAITING_KEY, userId);
    }
}
