package com.ticket.api.scheduler;

import com.ticket.api.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;

    @Value("${scheduler.queue.fetch-size:50}")
    private int fetchSize;

    @Scheduled(fixedDelayString = "${scheduler.queue.delay:1000}") // 딜레이도 설정으로 관리
    public void enterUsers() {
        queueService.allowUser(fetchSize);
    }
}