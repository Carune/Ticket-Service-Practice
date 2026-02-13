package com.ticket.api.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final String code;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final Map<String, String> errors;

    private ErrorResponse(String code, String message, String path, Instant timestamp, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, Instant.now(), null);
    }

    public static ErrorResponse of(String code, String message, String path, Map<String, String> errors) {
        return new ErrorResponse(code, message, path, Instant.now(), errors);
    }

}
