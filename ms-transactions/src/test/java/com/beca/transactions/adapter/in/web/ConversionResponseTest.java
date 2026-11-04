package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExchangeRate;
import com.beca.transactions.domain.model.ExchangeRates;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionResponseTest {

    private static Transaction transacao(ExchangeRates rates) {
        return new Transaction(1L, 1L, TransactionType.PURCHASE, new BigDecimal("150.90"), "Compra",
                Category.LAZER, null, null, TransactionStatus.APPROVED, null, rates, Instant.now());
    }

    @Test
    void from_deveMostrarOValorConvertidoComTaxaEData() {
        LocalDate data = LocalDate.of(2026, 11, 3);
        ExchangeRates rates = new ExchangeRates(
                new ExchangeRate("USD", new BigDecimal("5.4935"), data),
                new ExchangeRate("EUR", new BigDecimal("6.15"), data));

        ConversionResponse response = ConversionResponse.from(transacao(rates));

        assertThat(response.available()).isTrue();
        assertThat(response.reason()).isNull();
        assertThat(response.rates()).containsExactly(
                new ConversionResponse.ConvertedAmount("USD", new BigDecimal("5.4935"), data, new BigDecimal("27.47")),
                new ConversionResponse.ConvertedAmount("EUR", new BigDecimal("6.15"), data, new BigDecimal("24.54")));
    }

    @Test
    void from_deveSinalizarIndisponivelQuandoNaoHaTaxa() {
        ConversionResponse response = ConversionResponse.from(transacao(null));

        assertThat(response.available()).isFalse();
        assertThat(response.reason()).isNotBlank();
        assertThat(response.rates()).isEmpty();
    }
}
