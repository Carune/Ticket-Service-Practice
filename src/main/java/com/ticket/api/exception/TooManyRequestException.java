package com.ticket.api.exception;

// 429 Too Many Requests 처리를 위한 커스텀 예외
public class TooManyRequestException extends RuntimeException {
    public TooManyRequestException(String message) {
        super(message);
    }
}