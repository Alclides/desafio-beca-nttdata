package com.beca.transactions.adapter.out.persistence;

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

class TransactionPersistenceMapperTest {

    private static Transaction transacao(ExchangeRates rates) {
        return new Transaction(1L, 1L, TransactionType.PURCHASE, new BigDecimal("50.00"), "Compra",
                Category.LAZER, null, null, TransactionStatus.APPROVED, null, rates, Instant.now());
    }

    @Test
    void deveManterAsTaxasAoIrEVoltarDaEntity() {
        ExchangeRates rates = new ExchangeRates(
                new ExchangeRate("USD", new BigDecimal("5.4935"), LocalDate.of(2026, 11, 3)),
                new ExchangeRate("EUR", new BigDecimal("6.1500"), LocalDate.of(2026, 11, 2)));

        TransactionEntity entity = TransactionPersistenceMapper.toEntity(transacao(rates));

        assertThat(entity.getUsdRate()).isEqualByComparingTo("5.4935");
        assertThat(entity.getEurRateDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(TransactionPersistenceMapper.toDomain(entity).getExchangeRates()).isEqualTo(rates);
    }

    @Test
    void deveVoltarSemTaxaQuandoAsColunasEstaoVazias() {
        TransactionEntity entity = TransactionPersistenceMapper.toEntity(transacao(null));

        assertThat(entity.getUsdRate()).isNull();
        assertThat(TransactionPersistenceMapper.toDomain(entity).hasExchangeRates()).isFalse();
    }
}
