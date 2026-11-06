package com.beca.users.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AuthenticateRequest(
        @Schema(example = "antonia@gmail.com") @NotBlank String email,
        @Schema(example = "senha123") @NotBlank String password
) {
}
