package com.ticket.api.interceptor;

import com.ticket.api.annotation.QueuePass;
import com.ticket.api.service.QueueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

    private final QueueService queueService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청을 처리할 메서드가 HandlerMethod인지 확인
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 메서드에 @QueuePass 어노테이션이 붙어있는지 확인
        QueuePass queuePass = handlerMethod.getMethodAnnotation(QueuePass.class);
        if (queuePass == null) {
            return true; // 어노테이션이 없으면 그냥 통과
        }

        // 필터를 이미 통과했으므로 SecurityContext에 인증 정보가 있음
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String email = authentication.getName();
        log.info("대기열 통과 검증 시작 - User: {}", email);

        // 대기열 검증(입장권 없으면 에러)
        if (!queueService.isAllowed(email)) {
            log.warn("대기열 미통과 사용자 접근 차단 - User: {}", email);
            throw new IllegalStateException("대기열을 통과하지 못한 사용자입니다. 순서를 기다려주세요.");
        }

        return true; // 검증 통과 -> 컨트롤러 실행
    }
}
