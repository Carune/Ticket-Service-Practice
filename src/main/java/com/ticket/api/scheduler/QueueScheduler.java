package com.ticket.api.scheduler;

import com.ticket.api.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;

    // 1초(1000ms)마다 실행
    @Scheduled(fixedDelay = 1000)
    public void enterUsers() {
        // 한 번에 50명씩 입장 시킴
        // 1초에 50명이면 1분에 3000명 처리 가능
        queueService.allowUser(50);
    }
}