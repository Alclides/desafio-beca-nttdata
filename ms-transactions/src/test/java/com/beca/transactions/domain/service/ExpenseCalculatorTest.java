package com.beca.transactions.domain.service;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExpenseReport;
import com.beca.transactions.domain.model.Period;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseCalculatorTest {

    private static final Period NOVEMBRO = new Period(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    private static final Instant DIA_5 = Instant.parse("2026-11-05T15:00:00Z");

    private final ExpenseCalculator calculator = new ExpenseCalculator(ZoneId.of("America/Sao_Paulo"));

    private static Transaction transacao(Long id, TransactionType type, TransactionStatus status, String valor,
                                         Category category, Long corrige, Instant quando) {
        return new Transaction(id, 1L, type, new BigDecimal(valor), "Teste", category,
                type == TransactionType.TRANSFER ? 2L : null, corrige, status, null, null, quando);
    }

    private static Transaction compra(Long id, String valor, Category category) {
        return transacao(id, TransactionType.PURCHASE, TransactionStatus.APPROVED, valor, category, null, DIA_5);
    }

    private static Transaction compraEm(Long id, String valor, String quando) {
        return transacao(id, TransactionType.PURCHASE, TransactionStatus.APPROVED, valor, Category.LAZER, null,
                Instant.parse(quando));
    }

    @Test
    void byCategory_cincoComprasEmTresCategoriasDevemDarTresLinhas() {
        List<Transaction> compras = List.of(
                compra(1L, "100", Category.ALIMENTACAO),
                compra(2L, "50", Category.ALIMENTACAO),
                compra(3L, "30", Category.LAZER),
                compra(4L, "20", Category.TRANSPORTE),
                compra(5L, "10.50", Category.TRANSPORTE));

        ExpenseReport<Category> report = calculator.byCategory(NOVEMBRO, compras, Set.of());

        assertThat(report.totals()).hasSize(3);
        assertThat(report.totals().get(Category.ALIMENTACAO)).isEqualByComparingTo("150");
        assertThat(report.totals().get(Category.LAZER)).isEqualByComparingTo("30");
        assertThat(report.totals().get(Category.TRANSPORTE)).isEqualByComparingTo("30.50");
        assertThat(report.total()).isEqualByComparingTo("210.50");
        assertThat(report.period()).isEqualTo(NOVEMBRO);
    }

    @Test
    void deveContarRetiradaETransferenciaAprovadas() {
        List<Transaction> saidas = List.of(
                transacao(1L, TransactionType.WITHDRAWAL, TransactionStatus.APPROVED, "40", Category.OUTROS, null, DIA_5),
                transacao(2L, TransactionType.TRANSFER, TransactionStatus.APPROVED, "60", Category.OUTROS, null, DIA_5));

        assertThat(calculator.byCategory(NOVEMBRO, saidas, Set.of()).total()).isEqualByComparingTo("100");
    }

    @Test
    void deveIgnorarTransacoesRejeitadas() {
        List<Transaction> lista = List.of(
                compra(1L, "100", Category.LAZER),
                transacao(2L, TransactionType.PURCHASE, TransactionStatus.REJECTED, "800", Category.LAZER, null, DIA_5));

        assertThat(calculator.byCategory(NOVEMBRO, lista, Set.of()).total()).isEqualByComparingTo("100");
    }

    @Test
    void deveIgnorarDepositos() {
        List<Transaction> lista = List.of(
                compra(1L, "100", Category.LAZER),
                transacao(2L, TransactionType.DEPOSIT, TransactionStatus.APPROVED, "500", Category.OUTROS, null, DIA_5));

        assertThat(calculator.byCategory(NOVEMBRO, lista, Set.of()).total()).isEqualByComparingTo("100");
    }

    @Test
    void deveIgnorarOProprioEstorno() {
        // Estorno de depósito é uma retirada aprovada, mas não é despesa
        List<Transaction> lista = List.of(
                compra(1L, "100", Category.LAZER),
                transacao(2L, TransactionType.WITHDRAWAL, TransactionStatus.APPROVED, "40", Category.OUTROS, 9L, DIA_5));

        assertThat(calculator.byCategory(NOVEMBRO, lista, Set.of()).total()).isEqualByComparingTo("100");
    }

    @Test
    void deveIgnorarTransacaoEstornada() {
        List<Transaction> lista = List.of(
                compra(1L, "100", Category.ALIMENTACAO),
                compra(2L, "20", Category.TRANSPORTE));

        ExpenseReport<Category> report = calculator.byCategory(NOVEMBRO, lista, Set.of(2L));

        assertThat(report.totals()).containsOnlyKeys(Category.ALIMENTACAO);
        assertThat(report.total()).isEqualByComparingTo("100");
    }

    @Test
    void byDay_deveAgruparPorDiaEmOrdem() {
        List<Transaction> lista = List.of(
                compraEm(1L, "30", "2026-11-05T15:00:00Z"),
                compraEm(2L, "10", "2026-11-03T15:00:00Z"),
                compraEm(3L, "20", "2026-11-05T18:00:00Z"));

        ExpenseReport<LocalDate> report = calculator.byDay(NOVEMBRO, lista, Set.of());

        assertThat(report.totals().keySet()).containsExactly(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5));
        assertThat(report.totals().get(LocalDate.of(2026, 11, 5))).isEqualByComparingTo("50");
        assertThat(report.total()).isEqualByComparingTo("60");
    }

    @Test
    void byMonth_deveAgruparPorMesEmOrdem() {
        List<Transaction> lista = List.of(
                compraEm(1L, "30", "2026-11-05T15:00:00Z"),
                compraEm(2L, "10", "2026-10-20T15:00:00Z"),
                compraEm(3L, "20", "2026-11-25T15:00:00Z"));

        ExpenseReport<YearMonth> report = calculator.byMonth(NOVEMBRO, lista, Set.of());

        assertThat(report.totals().keySet()).containsExactly(YearMonth.of(2026, 10), YearMonth.of(2026, 11));
        assertThat(report.totals().get(YearMonth.of(2026, 11))).isEqualByComparingTo("50");
    }

    @Test
    void deveUsarOFusoDoBrasilParaDefinirODiaEOMes() {
        // 01:00 UTC do dia 1º de novembro ainda é 22:00 de 31 de outubro em São Paulo
        List<Transaction> lista = List.of(compraEm(1L, "10", "2026-11-01T01:00:00Z"));

        assertThat(calculator.byDay(NOVEMBRO, lista, Set.of()).totals()).containsOnlyKeys(LocalDate.of(2026, 10, 31));
        assertThat(calculator.byMonth(NOVEMBRO, lista, Set.of()).totals()).containsOnlyKeys(YearMonth.of(2026, 10));
    }

    @Test
    void deveDevolverTotalZeroSemDespesas() {
        ExpenseReport<Category> report = calculator.byCategory(NOVEMBRO, List.of(), Set.of());

        assertThat(report.totals()).isEmpty();
        assertThat(report.total()).isEqualByComparingTo("0");
    }
}
