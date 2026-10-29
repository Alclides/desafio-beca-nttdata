package com.beca.users.application.port.in;

import com.beca.users.domain.model.User;

public interface CreateUserUseCase {

    User create(CreateUserCommand command);

    record CreateUserCommand(String name, String email, String password) {
    }
}
