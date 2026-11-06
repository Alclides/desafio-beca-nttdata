package com.beca.users.application.port.in;

import com.beca.users.domain.model.User;

public interface AuthenticateUserUseCase {
    User authenticate(String email, String password);
}
