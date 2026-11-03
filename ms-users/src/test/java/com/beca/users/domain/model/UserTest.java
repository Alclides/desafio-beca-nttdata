package com.beca.users.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static User usuario(Long id, String email) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return new User(id, "Maria", email, "hash", now, now);
    }

    @Test
    void equals_deveConsiderarIguaisUsuariosComMesmoId() {
        // Igualdade é pela identidade (id), não pelos dados
        User a = usuario(1L, "maria@teste.com");
        User b = usuario(1L, "outro@teste.com");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void equals_deveConsiderarDiferentesUsuariosComIdsDiferentes() {
        assertThat(usuario(1L, "maria@teste.com")).isNotEqualTo(usuario(2L, "maria@teste.com"));
    }

    @Test
    void equals_deveSerDiferenteDeNullEDeOutroTipo() {
        User user = usuario(1L, "maria@teste.com");

        assertThat(user.equals(null)).isFalse();
        assertThat(user.equals("1")).isFalse();
    }
}
