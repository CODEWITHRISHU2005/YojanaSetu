package com.CodeWithRishu.YojanaSetu.utils;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(HttpStatus status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime(), status.value(), error, message, path);
    }
}