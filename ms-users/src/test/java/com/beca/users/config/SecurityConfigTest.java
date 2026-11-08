package com.beca.users.config;

import com.beca.users.adapter.in.security.UserAuthenticationProvider;
import com.beca.users.adapter.in.web.CsvUserImporter;
import com.beca.users.adapter.in.web.SecurityErrorResponder;
import com.beca.users.adapter.in.web.UserController;
import com.beca.users.application.port.in.AuthenticateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.DeleteUserUseCase;
import com.beca.users.application.port.in.FindUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase;
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
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Login de verdade com Basic, passando pelo provider (sem o atalho do TestLogin)
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserAuthenticationProvider.class, SecurityErrorResponder.class})
class SecurityConfigTest {

    private static final String EMAIL = "maria@teste.com";
    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private FindUserUseCase findUserUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private CsvUserImporter csvUserImporter;

    private static User maria() {
        Instant agora = Instant.now();
        return new User(1L, "Maria", EMAIL, "hash", agora, agora);
    }

    @Test
    void comCredenciaisCorretas_deveRetornar200() throws Exception {
        when(authenticateUserUseCase.authenticate(EMAIL, SENHA)).thenReturn(maria());
        when(findUserUseCase.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk());
    }

    @Test
    void comCredenciaisErradas_deveRetornar401EmJson() throws Exception {
        when(authenticateUserUseCase.authenticate(EMAIL, "errada"))
                .thenThrow(new InvalidCredentialsException("E-mail ou senha incorretos"));

        mockMvc.perform(get("/users").with(httpBasic(EMAIL, "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Informe e-mail e senha válidos"));

        verifyNoInteractions(findUserUseCase);
    }

    @Test
    void logadoComoMaria_naoPodeAlterarOutraConta() throws Exception {
        when(authenticateUserUseCase.authenticate(EMAIL, SENHA)).thenReturn(maria());

        mockMvc.perform(put("/users/2").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Outro", "email": "outro@teste.com"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(updateUserUseCase);
    }
}
