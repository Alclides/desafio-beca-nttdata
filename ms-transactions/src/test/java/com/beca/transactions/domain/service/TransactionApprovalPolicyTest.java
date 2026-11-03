package com.beca.transactions.domain.service;

import com.beca.transactions.domain.model.AccountBalance;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionApprovalPolicyTest {

    private final TransactionApprovalPolicy policy = new TransactionApprovalPolicy();

    // saldo 1000, limite diário 2000, limite por transação 500
    private static AccountBalance conta() {
        return new AccountBalance(1L, new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("500"));
    }

    private static Transaction compra(String valor) {
        return Transaction.create(1L, TransactionType.PURCHASE, new BigDecimal(valor), "Compra",
                Category.LAZER, null, null);
    }

    @Test
    void evaluate_deveAprovarDentroDeTodosOsLimites() {
        assertThat(policy.evaluate(compra("100"), conta(), BigDecimal.ZERO)).isEmpty();
    }

    @Test
    void evaluate_deveRejeitarAcimaDoLimitePorTransacao() {
        assertThat(policy.evaluate(compra("500.01"), conta(), BigDecimal.ZERO))
                .contains("Valor excede o limite por transação");
    }

    @Test
    void evaluate_deveAprovarValorIgualAoLimitePorTransacao() {
        assertThat(policy.evaluate(compra("500"), conta(), BigDecimal.ZERO)).isEmpty();
    }

    @Test
    void evaluate_deveRejeitarQuandoSomaDoDiaPassaDoLimiteDiario() {
        assertThat(policy.evaluate(compra("100"), conta(), new BigDecimal("1950")))
                .contains("Valor excede o limite diário");
    }

    @Test
    void evaluate_deveAprovarQuandoSomaDoDiaFicaIgualAoLimiteDiario() {
        AccountBalance saldoAlto = new AccountBalance(1L, new BigDecimal("5000"), new BigDecimal("2000"), new BigDecimal("500"));

        assertThat(policy.evaluate(compra("100"), saldoAlto, new BigDecimal("1900"))).isEmpty();
    }

    @Test
    void evaluate_deveRejeitarComSaldoInsuficiente() {
        AccountBalance saldoBaixo = new AccountBalance(1L, new BigDecimal("50"), new BigDecimal("2000"), new BigDecimal("500"));

        assertThat(policy.evaluate(compra("100"), saldoBaixo, BigDecimal.ZERO))
                .contains("Saldo insuficiente");
    }

    @Test
    void evaluate_deveAprovarValorIgualAoSaldo() {
        AccountBalance saldoJusto = new AccountBalance(1L, new BigDecimal("100"), new BigDecimal("2000"), new BigDecimal("500"));

        assertThat(policy.evaluate(compra("100"), saldoJusto, BigDecimal.ZERO)).isEmpty();
    }

    @Test
    void evaluate_deveCompararValoresIgnorandoCasasDecimais() {
        // 500.00 e 500 são o mesmo valor; com equals() seriam diferentes
        assertThat(policy.evaluate(compra("500.00"), conta(), BigDecimal.ZERO)).isEmpty();
    }

    @Test
    void evaluate_deveRetornarPrimeiroMotivoQuandoVariasRegrasFalham() {
        AccountBalance saldoBaixo = new AccountBalance(1L, new BigDecimal("50"), new BigDecimal("2000"), new BigDecimal("500"));

        assertThat(policy.evaluate(compra("800"), saldoBaixo, BigDecimal.ZERO))
                .contains("Valor excede o limite por transação");
    }
}
