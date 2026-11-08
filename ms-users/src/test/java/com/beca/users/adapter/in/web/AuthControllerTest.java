package com.beca.users.adapter.in.web;

import com.beca.users.adapter.in.security.UserAuthenticationProvider;
import com.beca.users.application.port.in.AuthenticateUserUseCase;
import com.beca.users.config.SecurityConfig;
import com.beca.users.domain.exception.InvalidCredentialsException;
import com.beca.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, UserAuthenticationProvider.class, SecurityErrorResponder.class})
class AuthControllerTest {

    private static final String BODY = """
            {"email": "maria@teste.com", "password": "senha123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUserUseCase useCase;

    @Test
    void authenticate_deveRetornar200SemASenha() throws Exception {
        Instant agora = Instant.now();
        when(useCase.authenticate("maria@teste.com", "senha123"))
                .thenReturn(new User(1L, "Maria", "maria@teste.com", "hash", agora, agora));

        mockMvc.perform(post("/users/authenticate").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void authenticate_deveRetornar401ComCredenciaisErradas() throws Exception {
        when(useCase.authenticate("maria@teste.com", "senha123"))
                .thenThrow(new InvalidCredentialsException("E-mail ou senha incorretos"));

        mockMvc.perform(post("/users/authenticate").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos"));
    }

    @Test
    void authenticate_deveRetornar400SemSenha() throws Exception {
        mockMvc.perform(post("/users/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"maria@teste.com\"}"))
                .andExpect(status().isBadRequest());
    }
}
