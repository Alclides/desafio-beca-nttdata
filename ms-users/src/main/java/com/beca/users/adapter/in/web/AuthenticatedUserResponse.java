package com.beca.users.adapter.in.web;

import com.beca.users.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticatedUserResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Antonia Rodrigues") String name,
        @Schema(example = "antonia@gmail.com") String email
) {
    static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
