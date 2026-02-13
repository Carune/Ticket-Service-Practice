package com.ticket.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.api.exception.ErrorResponse;
import com.ticket.api.jwt.JwtAuthenticationFilter;
import com.ticket.api.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // BCrypt를 스프링 빈으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 끄기 (Stateless)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", // Swagger
                                "/api/v1/auth/**",  // 로그인/회원가입 경로는 인증 없이 접근 가능
                                "/actuator/**" // 모니터링
                        ).permitAll()
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            res.setContentType("application/json");
                            ErrorResponse error = ErrorResponse.of(
                                    "UNAUTHORIZED",
                                    "인증이 필요합니다.",
                                    req.getRequestURI()
                            );
                            objectMapper.writeValue(res.getWriter(), error);
                        })
                );

        return http.build();
    }
}
