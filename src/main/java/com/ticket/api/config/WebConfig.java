package com.ticket.api.config;

import com.ticket.api.interceptor.QueueInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final QueueInterceptor queueInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queueInterceptor)
                .addPathPatterns("/api/v1/concerts/**") // 이 경로 하위만 검사
                .excludePathPatterns("/api/v1/auth/**"); // 예외 처리
    }
}