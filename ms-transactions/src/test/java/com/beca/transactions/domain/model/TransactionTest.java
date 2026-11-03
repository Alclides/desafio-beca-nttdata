package com.beca.transactions.domain.model;

import com.beca.transactions.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final BigDecimal CEM = new BigDecimal("100.00");

    private static Transaction novaRetirada() {
        return Transaction.create(1L, TransactionType.WITHDRAWAL, CEM, "Saque", null, null, null);
    }

    @Test
    void create_deveCriarTransacaoSemStatusESemId() {
        Transaction tx = Transaction.create(1L, TransactionType.PURCHASE, CEM, "Mercado",
                Category.ALIMENTACAO, null, null);

        assertThat(tx.getId()).isNull();
        assertThat(tx.getUserId()).isEqualTo(1L);
        assertThat(tx.getType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(tx.getAmount()).isEqualByComparingTo("100");
        assertThat(tx.getCategory()).isEqualTo(Category.ALIMENTACAO);
        assertThat(tx.getStatus()).isNull();
        assertThat(tx.getRejectedReason()).isNull();
        assertThat(tx.getCreatedAt()).isNotNull();
    }

    @Test
    void create_deveUsarCategoriaOutrosQuandoNaoInformada() {
        Transaction tx = Transaction.create(1L, TransactionType.DEPOSIT, CEM, "Salário", null, null, null);

        assertThat(tx.getCategory()).isEqualTo(Category.OUTROS);
    }

    @Test
    void create_deveAceitarTransferenciaParaOutroUsuario() {
        Transaction tx = Transaction.create(1L, TransactionType.TRANSFER, CEM, "Aluguel", null, 2L, null);

        assertThat(tx.getDestinationUserId()).isEqualTo(2L);
    }

    @Test
    void create_deveGuardarTransacaoCorrigida() {
        Transaction tx = Transaction.create(1L, TransactionType.DEPOSIT, CEM, "Estorno", null, null, 10L);

        assertThat(tx.getCorrectedTransactionId()).isEqualTo(10L);
    }

    @Test
    void create_deveFalharSemUsuario() {
        assertThatThrownBy(() -> Transaction.create(null, TransactionType.DEPOSIT, CEM, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Usuário é obrigatório");
    }

    @Test
    void create_deveFalharSemTipo() {
        assertThatThrownBy(() -> Transaction.create(1L, null, CEM, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Tipo da transação é obrigatório");
    }

    @Test
    void create_deveFalharComValorNulo() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.DEPOSIT, null, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Valor deve ser maior que zero");
    }

    @Test
    void create_deveFalharComValorZero() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.DEPOSIT, BigDecimal.ZERO, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Valor deve ser maior que zero");
    }

    @Test
    void create_deveFalharComValorNegativo() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.DEPOSIT, new BigDecimal("-1"), null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Valor deve ser maior que zero");
    }

    @Test
    void create_deveFalharTransferenciaSemDestino() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.TRANSFER, CEM, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transferência exige usuário de destino");
    }

    @Test
    void create_deveFalharTransferenciaParaSiMesmo() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.TRANSFER, CEM, null, null, 1L, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Não é possível transferir para si mesmo");
    }

    @Test
    void create_deveFalharDestinoEmTipoQueNaoETransferencia() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.WITHDRAWAL, CEM, null, null, 2L, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Usuário de destino só é permitido em transferência");
    }

    @Test
    void create_deveFalharCompraSemCategoria() {
        assertThatThrownBy(() -> Transaction.create(1L, TransactionType.PURCHASE, CEM, null, null, null, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Compra exige categoria");
    }

    @Test
    void approve_deveMarcarComoAprovada() {
        Transaction tx = novaRetirada();

        tx.approve();

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(tx.getRejectedReason()).isNull();
    }

    @Test
    void reject_deveMarcarComoRejeitadaComMotivo() {
        Transaction tx = novaRetirada();

        tx.reject("Saldo insuficiente");

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(tx.getRejectedReason()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void approve_deveFalharSeJaFoiAprovada() {
        Transaction tx = novaRetirada();
        tx.approve();

        assertThatThrownBy(tx::approve).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reject_deveFalharSeJaFoiAprovada() {
        Transaction tx = novaRetirada();
        tx.approve();

        assertThatThrownBy(() -> tx.reject("Saldo insuficiente")).isInstanceOf(IllegalStateException.class);
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    void approve_deveFalharSeJaFoiRejeitada() {
        Transaction tx = novaRetirada();
        tx.reject("Saldo insuficiente");

        assertThatThrownBy(tx::approve).isInstanceOf(IllegalStateException.class);
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.REJECTED);
    }
}
