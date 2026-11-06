package com.beca.transactions.adapter.in.security;

import com.beca.transactions.application.port.out.UserAuthenticationPort;
import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import com.beca.transactions.domain.model.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserServiceAuthenticationProvider implements AuthenticationProvider {

    private final UserAuthenticationPort userAuthenticationPort;

    public UserServiceAuthenticationProvider(UserAuthenticationPort userAuthenticationPort) {
        this.userAuthenticationPort = userAuthenticationPort;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            AuthenticatedUser user = userAuthenticationPort.authenticate(email, password)
                    .orElseThrow(() -> new BadCredentialsException("E-mail ou senha incorretos"));
            return UsernamePasswordAuthenticationToken.authenticated(user, null, List.of());
        } catch (UserServiceUnavailableException e) {
            throw new AuthenticationServiceException("Serviço de usuários indisponível", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
