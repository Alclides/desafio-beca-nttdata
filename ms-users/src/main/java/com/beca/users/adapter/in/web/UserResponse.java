package com.beca.users.adapter.in.web;

import com.beca.users.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UserResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Antonia Rodrigues") String name,
        @Schema(example = "antonia@gmail.com") String email,
        @Schema(example = "2026-11-03T18:00:00Z") Instant createdAt,
        @Schema(example = "2026-11-03T18:00:00Z") Instant updatedAt
) {

    static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
