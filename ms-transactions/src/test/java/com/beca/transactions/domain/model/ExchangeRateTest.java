package com.beca.transactions.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 3);

    @Test
    void convert_deveDividirPelaTaxaComDuasCasas() {
        BigDecimal valor = new BigDecimal("150.90");

        assertThat(new ExchangeRate("USD", new BigDecimal("5.4935"), DATA).convert(valor))
                .isEqualTo(new BigDecimal("27.47"));
        assertThat(new ExchangeRate("EUR", new BigDecimal("6.15"), DATA).convert(valor))
                .isEqualTo(new BigDecimal("24.54"));
    }
}
