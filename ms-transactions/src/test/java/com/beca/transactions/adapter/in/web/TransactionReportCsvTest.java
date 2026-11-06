package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionReportCsvTest {

    private static final String CABECALHO = "data;tipo;categoria;descrição;valor em real;status;motivo";
    private static final Instant MEIO_DIA_DE_05_11 = Instant.parse("2026-11-05T15:00:00Z");

    private static Transaction transacao(String valor, String descricao, TransactionStatus status, String motivo,
                                         Instant data) {
        return new Transaction(5L, 1L, TransactionType.PURCHASE, new BigDecimal(valor), descricao, Category.LAZER,
                null, null, status, motivo, null, data);
    }

    private static Transaction aprovada(String valor, String descricao) {
        return transacao(valor, descricao, TransactionStatus.APPROVED, null, MEIO_DIA_DE_05_11);
    }

    private static String csv(Transaction... transacoes) {
        return new String(TransactionReportCsv.toCsv(List.of(transacoes)), StandardCharsets.UTF_8);
    }

    // Linha do arquivo sem o BOM e sem a quebra de linha; a 0 é o cabeçalho
    private static String linha(int indice, Transaction... transacoes) {
        return csv(transacoes).substring(1).split("\r\n")[indice];
    }

    @Test
    void toCsv_deveComecarComOsBytesDoBom() {
        byte[] bytes = TransactionReportCsv.toCsv(List.of());

        assertThat(bytes).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }

    @Test
    void toCsv_deveTrazerSoBomECabecalhoQuandoNaoHaTransacoes() {
        assertThat(csv()).isEqualTo("﻿" + CABECALHO + "\r\n");
    }

    @Test
    void toCsv_deveEscreverAsColunasNaOrdemDoPedido() {
        assertThat(linha(1, aprovada("150.50", "Cinema")))
                .isEqualTo("05/11/2026;PURCHASE;LAZER;Cinema;150,50;APPROVED;");
    }

    @Test
    void toCsv_deveEscreverUmaLinhaPorTransacao() {
        String[] linhas = csv(aprovada("10.00", "Primeira"), aprovada("20.00", "Segunda")).split("\r\n");

        assertThat(linhas).hasSize(3);
        assertThat(linhas[1]).contains("Primeira");
        assertThat(linhas[2]).contains("Segunda");
    }

    @Test
    void toCsv_devePreservarAcentos() {
        assertThat(linha(1, aprovada("10.00", "Almoço na padaria São João")))
                .contains("Almoço na padaria São João");
    }

    @Test
    void toCsv_deveColocarDescricaoComSeparadorEntreAspas() {
        assertThat(linha(1, aprovada("10.00", "Mercado; padaria")))
                .contains(";\"Mercado; padaria\";");
    }

    @Test
    void toCsv_deveDeixarDescricaoVaziaQuandoNaoInformada() {
        assertThat(linha(1, aprovada("10.00", null)))
                .isEqualTo("05/11/2026;PURCHASE;LAZER;;10,00;APPROVED;");
    }

    @Test
    void toCsv_deveEscreverValorSemNotacaoCientifica() {
        assertThat(linha(1, aprovada("1E+3", "Aluguel")))
                .contains(";1000;");
    }

    @Test
    void toCsv_deveTrazerOMotivoDaTransacaoRejeitada() {
        Transaction rejeitada = transacao("10.00", "Cinema", TransactionStatus.REJECTED,
                "Saldo insuficiente", MEIO_DIA_DE_05_11);

        assertThat(linha(1, rejeitada))
                .isEqualTo("05/11/2026;PURCHASE;LAZER;Cinema;10,00;REJECTED;Saldo insuficiente");
    }

    @Test
    void toCsv_deveUsarADataDeSaoPauloENaoADeUtc() {
        // 01:00 de 06/11 em UTC ainda é 22:00 de 05/11 em São Paulo
        Transaction noiteEmSaoPaulo = transacao("10.00", "Cinema", TransactionStatus.APPROVED, null,
                Instant.parse("2026-11-06T01:00:00Z"));

        assertThat(linha(1, noiteEmSaoPaulo)).startsWith("05/11/2026;");
    }
}
