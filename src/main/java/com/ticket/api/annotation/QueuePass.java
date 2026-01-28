package com.ticket.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메서드 위에 붙이는 용도
@Retention(RetentionPolicy.RUNTIME) // 실행 중에도 동작
public @interface QueuePass {
}