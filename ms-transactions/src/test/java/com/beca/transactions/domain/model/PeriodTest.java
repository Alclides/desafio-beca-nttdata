package com.beca.transactions.domain.model;

import com.beca.transactions.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 5);

    @Test
    void resolve_deveUsarOMesAtualSemDatas() {
        assertThat(Period.resolve(null, null, HOJE))
                .isEqualTo(new Period(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)));
    }

    @Test
    void resolve_deveConsiderarOTamanhoDeFevereiro() {
        assertThat(Period.resolve(null, null, LocalDate.of(2026, 2, 10)).to()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(Period.resolve(null, null, LocalDate.of(2028, 2, 10)).to()).isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    void resolve_deveManterAsDatasInformadas() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 20);

        assertThat(Period.resolve(from, to, HOJE)).isEqualTo(new Period(from, to));
    }

    @Test
    void resolve_deveCompletarComOMesAtualQuandoFaltaUmaData() {
        assertThat(Period.resolve(LocalDate.of(2026, 11, 3), null, HOJE).to()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(Period.resolve(null, LocalDate.of(2026, 11, 3), HOJE).from()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void resolve_deveAceitarUmUnicoDia() {
        assertThat(Period.resolve(HOJE, HOJE, HOJE)).isEqualTo(new Period(HOJE, HOJE));
    }

    @Test
    void resolve_deveFalharComPeriodoInvertido() {
        assertThatThrownBy(() -> Period.resolve(LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), HOJE))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Data inicial não pode ser depois da data final");
    }
}
