package com.ticket.api.controller;

import com.ticket.api.service.QueueService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.*;

@Tag(name = "대기열 API", description = "대기열 등록, 순번 조회, 대기열 이탈 기능")
@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WAITING_KEY = "waiting_queue";
    private static final String ACTIVE_KEY_PREFIX = "active:user:";

    @Operation(summary = "대기열 등록", description = "대기열에 진입하여 대기표를 발급받습니다.")
    /*@PostMapping
    public ResponseEntity<String> addToQueue(Principal principal) {
        // JwtTokenProvider에서 setSubject(userId)로 넣었으므로 principal.getName()은 ID값
        String userId = principal.getName();

        queueService.addQueue(userId);

        return ResponseEntity.ok("대기열 등록 완료. User ID: " + userId);
    }*/

    // [테스트용] @RequestBody로 userId를 직접 받음 (부하테스트용)
    @PostMapping
    public ResponseEntity<String> addToQueue(@RequestBody Map<String, String> request) {
        String userId = request.get("userId"); // k6가 보낸 userId 사용
        queueService.addQueue(userId);
        return ResponseEntity.ok("대기열 등록 완료");
    }

    @Operation(summary = "내 대기 순번 조회", description = "현재 나의 대기 순번을 확인합니다.")
    @GetMapping("/rank")
    public ResponseEntity<String> getRank(Principal principal) {
        String userId = principal.getName();

        Long rank = queueService.getRank(userId);
        if (rank == -1) {
            return ResponseEntity.ok("대기열에 없는 유저입니다.");
        }
        return ResponseEntity.ok("현재 대기 순번: " + rank + "등");
    }

    @Operation(summary = "대기열 취소(이탈)", description = "대기하다가 포기하고 나가는 경우 대기열에서 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<String> cancelQueue(Principal principal) {
        String userId = principal.getName();
        queueService.removeQueue(userId);
        return ResponseEntity.ok("대기열에서 취소되었습니다.");
    }

    // [테스트] 더미 유저 1000명 대기열
    @Profile("!prod")
    @PostMapping("/dummy")
    public ResponseEntity<String> addDummy() {
        for (int i = 0; i < 10000; i++) {
            queueService.addQueue("dummy_" + i);
        }

        return ResponseEntity.ok("Pipeline 완료");
    }

    @PostMapping("/redis/pipeline")
    public ResponseEntity<String> pipeline() {
        // 1. [준비] 기존 데이터 싹 비우기
        redisTemplate.delete(WAITING_KEY);
        Set<String> keys = redisTemplate.keys(ACTIVE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        System.out.println("--- [pipeline] 데이터 세팅 시작 (1만명) ---");
        int TOTAL_COUNT = 10000;
        int BATCH_SIZE = 1000;

        for (int i = 0; i < TOTAL_COUNT; i += BATCH_SIZE) {
            int startIdx = i;
            int endIdx = Math.min(i + BATCH_SIZE, TOTAL_COUNT);

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byte[] keyBytes = WAITING_KEY.getBytes();
                for (int j = startIdx; j < endIdx; j++) {
                    connection.zAdd(keyBytes, System.currentTimeMillis(), ("user_" + j).getBytes());
                }
                return null;
            });
        }
        System.out.println("--- [pipeline] 데이터 세팅 완료 (1만명) ---");

        long start = System.currentTimeMillis();

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().popMin(WAITING_KEY, TOTAL_COUNT);

        if (tuples != null && !tuples.isEmpty()) {
            // 3-2. Pipeline으로 처리 (여기도 Batch 적용!)
            List<ZSetOperations.TypedTuple<String>> tupleList = new ArrayList<>(tuples);

            for (int i = 0; i < tupleList.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, tupleList.size());
                List<ZSetOperations.TypedTuple<String>> batch = tupleList.subList(i, end);

                redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (ZSetOperations.TypedTuple<String> tuple : batch) {
                        String userId = tuple.getValue();
                        String key = ACTIVE_KEY_PREFIX + userId;
                        connection.stringCommands().setEx(key.getBytes(), 300, "true".getBytes());
                    }
                    return null;
                });
            }
        }

        long end = System.currentTimeMillis();
        long duration = end - start;

        String resultLog = String.format("[최종 결과] 1만 명 처리 소요 시간: %d ms (%.2f초)", duration, duration / 1000.0);
        System.out.println(resultLog);

        return ResponseEntity.ok(resultLog);
    }

    @PostMapping("/redis/bad-loop")
    public ResponseEntity<String> badLoop() {
        redisTemplate.delete(WAITING_KEY);
        Set<String> keys = redisTemplate.keys(ACTIVE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // [등록] 더미 데이터 넣기
        System.out.println("--- [For] 데이터 세팅 시작 (1만명) ---");
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForZSet().add(WAITING_KEY, "user_" + i, System.currentTimeMillis());
        }
        System.out.println("--- [For] 데이터 세팅 완료 (1만명) ---");

        long start = System.currentTimeMillis();

        // 꺼내기
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().popMin(WAITING_KEY, 10000);

        if (tuples != null && !tuples.isEmpty()) {
            // For문으로 하나씩 처리
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String userId = tuple.getValue();
                String key = ACTIVE_KEY_PREFIX + userId;

                // 매번 Redis에 접속해서 저장
                redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(5));
            }
        }

        long end = System.currentTimeMillis();
        long duration = end - start;

        String resultLog = String.format("[For 결과] 1만 명 Loop 처리 소요 시간: %d ms (%.2f초)", duration, duration / 1000.0);
        System.out.println(resultLog);

        return ResponseEntity.ok(resultLog);
    }
}