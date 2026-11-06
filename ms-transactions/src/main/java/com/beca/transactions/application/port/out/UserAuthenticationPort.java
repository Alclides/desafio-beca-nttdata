package com.beca.transactions.application.port.out;

import com.beca.transactions.domain.model.AuthenticatedUser;

import java.util.Optional;

public interface UserAuthenticationPort {
    // Vazio quando e-mail ou senha estão errados; UserServiceUnavailableException quando o MS1 não responde
    Optional<AuthenticatedUser> authenticate(String email, String password);
}
