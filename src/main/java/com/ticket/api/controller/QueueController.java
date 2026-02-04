package com.ticket.api.controller;

import com.ticket.api.service.QueueService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Tag(name = "대기열 API", description = "대기열 등록, 순번 조회, 대기열 이탈 기능")
@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @Operation(summary = "대기열 등록", description = "대기열에 진입하여 대기표를 발급받습니다.")
    @PostMapping
    public ResponseEntity<String> addToQueue(Principal principal) {
        // JwtTokenProvider에서 setSubject(userId)로 넣었으므로 principal.getName()은 ID값
        String userId = principal.getName();

        queueService.addQueue(userId);

        return ResponseEntity.ok("대기열 등록 완료. User ID: " + userId);
    }

    // [테스트용] @RequestBody로 userId를 직접 받음 (부하테스트용)
    /*@PostMapping
    public ResponseEntity<String> addToQueue(@RequestBody Map<String, String> request) {
        String userId = request.get("userId"); // k6가 보낸 userId 사용
        queueService.addQueue(userId);
        return ResponseEntity.ok("대기열 등록 완료");
    }*/

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
    @Hidden
    @Profile("!prod")
    @PostMapping("/dummy")
    public ResponseEntity<String> addDummy() {
        for (int i = 0; i < 1000; i++) {
            queueService.addQueue("dummy_" + i);
        }
        return ResponseEntity.ok("더미 유저 1000명 대기열 등록 완료");
    }
}