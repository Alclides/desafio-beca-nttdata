package com.beca.users.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 72) String password
) {
}
