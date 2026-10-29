package com.beca.users.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter();

    @Test
    void encode_deveGerarHashBCryptValido() {
        String hash = adapter.encode("123456");

        assertThat(hash).isNotEqualTo("123456").startsWith("$2a$");
        assertThat(new BCryptPasswordEncoder().matches("123456", hash)).isTrue();
    }

    @Test
    void encode_deveGerarHashesDiferentesParaMesmaSenha() {
        assertThat(adapter.encode("123456")).isNotEqualTo(adapter.encode("123456"));
    }
}
