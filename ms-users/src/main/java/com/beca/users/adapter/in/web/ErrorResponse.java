package com.beca.users.adapter.in.web;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fields,
        Instant timestamp
) {

    static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null, Instant.now());
    }

    static ErrorResponse of(int status, String error, String message, Map<String, String> fields) {
        return new ErrorResponse(status, error, message, fields, Instant.now());
    }
}
