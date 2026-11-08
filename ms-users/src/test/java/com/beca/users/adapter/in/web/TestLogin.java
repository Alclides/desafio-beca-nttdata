package com.beca.users.adapter.in.web;

import com.beca.users.adapter.in.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

// Simula nos testes o usuário logado, do mesmo jeito que o provider de login faz
final class TestLogin {

    private TestLogin() {
    }

    static RequestPostProcessor as(Long userId) {
        AuthenticatedUser user = new AuthenticatedUser(userId, "Teste", "teste@teste.com");
        return authentication(UsernamePasswordAuthenticationToken.authenticated(user, null, List.of()));
    }
}
