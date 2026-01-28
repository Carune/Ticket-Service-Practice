package com.ticket.api.controller;

import com.ticket.api.service.ConcertService;
import com.ticket.api.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final ConcertService concertService;

    @PostMapping
    public ResponseEntity<String> addToQueue(Principal principal) {
        // JwtTokenProvider에서 setSubject(userId)로 넣었으므로 principal.getName()은 ID값
        String userId = principal.getName();

        queueService.addQueue(userId);

        return ResponseEntity.ok("대기열 등록 완료. User ID: " + userId);
    }

    @GetMapping("/rank")
    public ResponseEntity<String> getRank(Principal principal) {
        String userId = principal.getName();

        Long rank = queueService.getRank(userId);
        if (rank == -1) {
            return ResponseEntity.ok("대기열에 없는 유저입니다.");
        }
        return ResponseEntity.ok("현재 대기 순번: " + rank + "등");
    }

    // [테스트] 더미 유저 1000명 대기열
    @PostMapping("/dummy")
    public ResponseEntity<String> addDummy() {
        for (int i = 0; i < 1000; i++) {
            queueService.addQueue("dummy_" + i);
        }
        return ResponseEntity.ok("더미 유저 1000명 대기열 등록 완료");
    }
}