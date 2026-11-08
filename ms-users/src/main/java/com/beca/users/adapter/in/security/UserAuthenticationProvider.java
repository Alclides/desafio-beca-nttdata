package com.beca.users.adapter.in.security;

import com.beca.users.application.port.in.AuthenticateUserUseCase;
import com.beca.users.domain.exception.InvalidCredentialsException;
import com.beca.users.domain.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

// Confere o Basic usando o mesmo caso de uso do POST /users/authenticate
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public UserAuthenticationProvider(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            User user = authenticateUserUseCase.authenticate(email, password);
            AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getName(), user.getEmail());
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
        } catch (InvalidCredentialsException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
