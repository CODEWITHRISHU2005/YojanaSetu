package com.CodeWithRishu.YojanaSetu.utils;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(HttpStatus status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(java.time.ZoneOffset.UTC), status.value(), error, message, path);
    }
}