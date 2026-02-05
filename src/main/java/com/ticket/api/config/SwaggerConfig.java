package com.ticket.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 설정 (JWT 토큰)
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // Bearer Token

        return new OpenAPI()
                .info(new Info()
                        .title("콘서트 대기열 예매 시스템 API")
                        .version("v1.0.0")
                        .description("""
                                ## 프로젝트 개요
                                대규모 트래픽을 감당하는 콘서트 티켓팅 서비스의 백엔드 API 문서입니다
                                
                                ### 기술 스택
                                - **Architecture**: Layered Architecture
                                - **Core**: Spring Boot 3.2, Java 17
                                - **Database**: MySQL 8.0, Redis
                                - **Infra**: AWS EC2, Docker Compose, Github Actions
                                
                                ### 주요 기능
                                - **대기열 시스템**: Redis Sorted Set을 이용한 고성능 대기열 (Polling 방식)
                                - **동시성 제어**: 좌석 예약 시 낙관적 락(Optimistic Lock) 적용
                                - **성능 최적화**: Look-aside 캐싱 및 Redis Pipelining 적용
                                """)
                        .contact(new Contact()
                                .name("Github 방문하기")
                                .url("https://github.com/Carune/Ticket-Service-Practice.git")))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}