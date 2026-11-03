package com.beca.users.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserImportResponse(
        @Schema(example = "5", description = "Linhas de dados lidas do arquivo") int total,
        @Schema(example = "4") int successes,
        @Schema(example = "1") int failures,
        @Schema(description = "Linhas que não entraram, com o motivo") List<LineError> errors
) {

    public record LineError(
            @Schema(example = "3", description = "Número da linha no arquivo") long line,
            @Schema(example = "antonia@gmail.com") String email,
            @Schema(example = "E-mail já cadastrado: antonia@gmail.com") String reason
    ) {
    }
}
