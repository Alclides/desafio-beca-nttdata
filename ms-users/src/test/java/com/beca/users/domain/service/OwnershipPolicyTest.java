package com.beca.users.domain.service;

import com.beca.users.domain.exception.ResourceAccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipPolicyTest {

    @Test
    void ensureOwner_devePassarQuandoEOMesmoUsuario() {
        assertThatCode(() -> OwnershipPolicy.ensureOwner(1L, 1L)).doesNotThrowAnyException();
    }

    @Test
    void ensureOwner_deveNegarQuandoEOutroUsuario() {
        assertThatThrownBy(() -> OwnershipPolicy.ensureOwner(1L, 2L))
                .isInstanceOf(ResourceAccessDeniedException.class)
                .hasMessage("Acesso negado: só é possível alterar a própria conta");
    }
}
