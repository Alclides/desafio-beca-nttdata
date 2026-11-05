package com.beca.users.adapter.out.persistence;

import com.beca.users.domain.exception.EmailAlreadyExistsException;
import com.beca.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository repository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    private static User novoUsuario() {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return new User(null, "Maria", "maria@teste.com", "hash", now, now);
    }

    @Test
    void save_deveRetornarUsuarioComIdGerado() {
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        User saved = adapter.save(novoUsuario());

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getEmail()).isEqualTo("maria@teste.com");
    }

    @Test
    void save_deveTraduzirViolacaoDeUnicidadeEmEmailJaCadastrado() {
        // Simula a segunda de duas requisições simultâneas: o banco recusa pela constraint unique
        when(repository.save(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> adapter.save(novoUsuario()))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("E-mail já cadastrado: maria@teste.com");
    }
}
