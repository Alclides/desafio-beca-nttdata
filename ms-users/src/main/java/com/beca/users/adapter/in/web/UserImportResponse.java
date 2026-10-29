package com.beca.users.adapter.in.web;

import java.util.List;

public record UserImportResponse(
        int total,
        int successes,
        int failures,
        List<LineError> errors
) {

    public record LineError(long line, String email, String reason) {
    }
}
