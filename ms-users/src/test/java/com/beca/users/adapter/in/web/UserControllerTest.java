package com.beca.users.adapter.in.web;

import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.application.port.in.DeleteUserUseCase;
import com.beca.users.application.port.in.FindUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import com.beca.users.domain.exception.UserNotFoundException;
import com.beca.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private FindUserUseCase findUserUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    private static User user(Long id, String name, String email) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return new User(id, name, email, "hash", now, now);
    }

    @Test
    void create_deveRetornar201ComLocationESemSenha() throws Exception {
        when(createUserUseCase.create(new CreateUserCommand("Maria", "maria@teste.com", "123456")))
                .thenReturn(user(1L, "Maria", "maria@teste.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria", "email": "maria@teste.com", "password": "123456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@teste.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void create_deveRetornar400QuandoDadosInvalidos() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": "email-invalido", "password": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());

        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void create_deveRetornar409QuandoEmailJaExiste() throws Exception {
        when(createUserUseCase.create(any()))
                .thenThrow(new EmailAlreadyExistsException("E-mail já cadastrado: maria@teste.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria", "email": "maria@teste.com", "password": "123456"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado: maria@teste.com"));
    }

    @Test
    void findById_deveRetornar200() throws Exception {
        when(findUserUseCase.findById(1L)).thenReturn(user(1L, "Maria", "maria@teste.com"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void findById_deveRetornar404QuandoNaoExiste() throws Exception {
        when(findUserUseCase.findById(99L)).thenThrow(new UserNotFoundException("Usuário não encontrado: 99"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuário não encontrado: 99"));
    }

    @Test
    void findAll_deveRetornarListaDeUsuarios() throws Exception {
        when(findUserUseCase.findAll()).thenReturn(List.of(
                user(1L, "Maria", "maria@teste.com"),
                user(2L, "João", "joao@teste.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].name").value("João"));
    }

    @Test
    void update_deveRetornar200() throws Exception {
        when(updateUserUseCase.update(eq(1L), eq(new UpdateUserCommand("Maria Silva", "novo@teste.com"))))
                .thenReturn(user(1L, "Maria Silva", "novo@teste.com"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria Silva", "email": "novo@teste.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("novo@teste.com"));
    }

    @Test
    void update_deveRetornar400QuandoDadosInvalidos() throws Exception {
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.email").exists());

        verifyNoInteractions(updateUserUseCase);
    }

    @Test
    void delete_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(deleteUserUseCase).delete(1L);
    }

    @Test
    void delete_deveRetornar404QuandoNaoExiste() throws Exception {
        doThrow(new UserNotFoundException("Usuário não encontrado: 99")).when(deleteUserUseCase).delete(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
