package com.beca.users.application.port.in;

import com.beca.users.domain.model.User;

public interface UpdateUserUseCase {

    User update(Long id, UpdateUserCommand command);

    record UpdateUserCommand(String name, String email) {
    }
}
