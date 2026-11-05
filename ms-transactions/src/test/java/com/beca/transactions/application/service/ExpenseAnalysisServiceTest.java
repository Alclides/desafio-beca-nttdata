package com.beca.transactions.application.service;

import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.exception.InvalidTransactionException;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExpenseReport;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import com.beca.transactions.domain.service.ExpenseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalysisServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate FROM = LocalDate.of(2026, 11, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 30);

    @Mock
    private TransactionRepositoryPort repository;

    private ExpenseAnalysisService service;

    @BeforeEach
    void setUp() {
        // O calculador é o real, para o teste exercitar as regras de verdade
        service = new ExpenseAnalysisService(repository, new ExpenseCalculator(ZONE));
    }

    private static Transaction compra(Long id, String valor, Category category) {
        return new Transaction(id, 1L, TransactionType.PURCHASE, new BigDecimal(valor), "Compra", category,
                null, null, TransactionStatus.APPROVED, null, null, Instant.parse("2026-11-05T15:00:00Z"));
    }

    @Test
    void byCategory_deveBuscarOPeriodoInteiroNoFusoDoBrasil() {
        when(repository.findByUser(eq(1L), any(), any(), isNull())).thenReturn(List.of());
        when(repository.findReversedTransactionIds(1L)).thenReturn(Set.of());

        service.byCategory(1L, FROM, TO);

        // Fim exclusivo = meia-noite do dia seguinte, para as 23:59 do dia 30 entrarem
        verify(repository).findByUser(1L,
                FROM.atStartOfDay(ZONE).toInstant(),
                LocalDate.of(2026, 12, 1).atStartOfDay(ZONE).toInstant(),
                null);
    }

    @Test
    void byCategory_deveUsarOMesAtualSemPeriodo() {
        LocalDate hoje = LocalDate.now(ZONE);
        when(repository.findByUser(eq(1L), any(), any(), isNull())).thenReturn(List.of());
        when(repository.findReversedTransactionIds(1L)).thenReturn(Set.of());

        ExpenseReport<Category> report = service.byCategory(1L, null, null);

        assertThat(report.period().from()).isEqualTo(hoje.withDayOfMonth(1));
        assertThat(report.period().to()).isEqualTo(hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }

    @Test
    void byCategory_deveTirarOsIdsEstornados() {
        when(repository.findByUser(eq(1L), any(), any(), isNull()))
                .thenReturn(List.of(compra(1L, "100", Category.ALIMENTACAO), compra(2L, "20", Category.TRANSPORTE)));
        when(repository.findReversedTransactionIds(1L)).thenReturn(Set.of(2L));

        ExpenseReport<Category> report = service.byCategory(1L, FROM, TO);

        assertThat(report.totals()).containsOnlyKeys(Category.ALIMENTACAO);
        assertThat(report.total()).isEqualByComparingTo("100");
    }

    @Test
    void byDay_deveAgruparPorDia() {
        when(repository.findByUser(eq(1L), any(), any(), isNull())).thenReturn(List.of(compra(1L, "100", Category.LAZER)));
        when(repository.findReversedTransactionIds(1L)).thenReturn(Set.of());

        assertThat(service.byDay(1L, FROM, TO).totals()).containsOnlyKeys(LocalDate.of(2026, 11, 5));
    }

    @Test
    void byMonth_deveAgruparPorMes() {
        when(repository.findByUser(eq(1L), any(), any(), isNull())).thenReturn(List.of(compra(1L, "100", Category.LAZER)));
        when(repository.findReversedTransactionIds(1L)).thenReturn(Set.of());

        assertThat(service.byMonth(1L, FROM, TO).totals()).containsOnlyKeys(YearMonth.of(2026, 11));
    }

    @Test
    void deveFalharComPeriodoInvertidoSemConsultarOBanco() {
        assertThatThrownBy(() -> service.byCategory(1L, TO, FROM))
                .isInstanceOf(InvalidTransactionException.class);

        verifyNoInteractions(repository);
    }
}
