package com.beca.users.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(example = "Antonia Rodrigez") @NotBlank @Size(max = 100) String name,
        @Schema(example = "Antonia@gmail.com")@NotBlank @Email String email,
        @Schema(example = "senha123 - NÃO COPIE")@NotBlank @Size(min = 6, max = 72) String password
) {
}
