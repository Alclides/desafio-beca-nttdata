package com.beca.transactions.domain.service;

import com.beca.transactions.domain.exception.ResourceAccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipPolicyTest {

    @Test
    void ensureOwner_devePermitirOProprioUsuario() {
        // Ids fora da faixa de cache do Long: garante que a comparação é por valor
        assertThatCode(() -> OwnershipPolicy.ensureOwner(Long.valueOf(1000), Long.valueOf(1000)))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureOwner_deveNegarOutroUsuario() {
        assertThatThrownBy(() -> OwnershipPolicy.ensureOwner(1L, 2L))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }
}
