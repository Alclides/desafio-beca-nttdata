package com.beca.transactions.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

// Mesmo formato do ms-users: os dois serviços respondem erros do mesmo jeito
public record ErrorResponse(
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Dados inválidos") String message,
        @Schema(description = "Campo → mensagem; só aparece em erro de validação") Map<String, String> fields,
        @Schema(example = "2026-11-03T18:00:00Z") Instant timestamp
) {

    static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null, Instant.now());
    }

    static ErrorResponse of(int status, String error, String message, Map<String, String> fields) {
        return new ErrorResponse(status, error, message, fields, Instant.now());
    }
}
