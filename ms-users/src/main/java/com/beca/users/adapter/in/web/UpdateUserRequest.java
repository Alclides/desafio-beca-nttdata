package com.beca.users.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Schema(example = "Antonia Rodrigues") @NotBlank @Size(max = 100) String name,
        @Schema(example = "antonia@gmail.com") @NotBlank @Email String email
) {}
