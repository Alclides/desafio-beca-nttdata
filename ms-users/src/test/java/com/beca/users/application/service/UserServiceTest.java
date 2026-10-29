package com.beca.users.application.service;

import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.beca.users.application.port.out.PasswordEncoderPort;
import com.beca.users.application.port.out.UserRepositoryPort;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import com.beca.users.domain.exception.UserNotFoundException;
import com.beca.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserService service;

    private static User existingUser() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        return new User(1L, "Maria", "maria@teste.com", "hash", createdAt, createdAt);
    }

    @Test
    void create_deveSalvarUsuarioComSenhaCriptografada() {
        when(repository.existsByEmail("maria@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.create(new CreateUserCommand("Maria", "maria@teste.com", "123456"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getName()).isEqualTo("Maria");
        assertThat(saved.getEmail()).isEqualTo("maria@teste.com");
        assertThat(saved.getPassword()).isEqualTo("hash");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(created).isSameAs(saved);
    }

    @Test
    void create_deveLancarExcecaoQuandoEmailJaExiste() {
        when(repository.existsByEmail("maria@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserCommand("Maria", "maria@teste.com", "123456")))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("maria@teste.com");

        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void findById_deveRetornarUsuarioQuandoExiste() {
        User user = existingUser();
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.findById(1L)).isSameAs(user);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_deveRetornarTodosOsUsuarios() {
        List<User> users = List.of(existingUser());
        when(repository.findAll()).thenReturn(users);

        assertThat(service.findAll()).isEqualTo(users);
    }

    @Test
    void update_deveAtualizarNomeEEmail() {
        User user = existingUser();
        Instant previousUpdatedAt = user.getUpdatedAt();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("novo@teste.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = service.update(1L, new UpdateUserCommand("Maria Silva", "novo@teste.com"));

        assertThat(updated.getName()).isEqualTo("Maria Silva");
        assertThat(updated.getEmail()).isEqualTo("novo@teste.com");
        assertThat(updated.getPassword()).isEqualTo("hash");
        assertThat(updated.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void update_naoDeveValidarEmailQuandoNaoMudou() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = service.update(1L, new UpdateUserCommand("Maria Silva", "maria@teste.com"));

        assertThat(updated.getName()).isEqualTo("Maria Silva");
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    void update_deveLancarExcecaoQuandoNovoEmailJaExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(repository.existsByEmail("outro@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateUserCommand("Maria", "outro@teste.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateUserCommand("Maria", "maria@teste.com")))
                .isInstanceOf(UserNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_deveRemoverUsuarioQuandoExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(repository, never()).deleteById(anyLong());
    }
}
