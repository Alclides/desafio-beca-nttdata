package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
  Gera o relatório de transações em CSV, no formato que o Excel em português abre direto:
  separador ponto e vírgula, UTF-8 com BOM e valor com vírgula decimal.
 */
final class TransactionReportCsv {

    private static final char BOM = '﻿';
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader("data", "tipo", "categoria", "descrição", "valor em real", "status", "motivo")
            .build();

    private TransactionReportCsv() {
    }

    static byte[] toCsv(List<Transaction> transactions) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(BOM);
            try (CSVPrinter printer = new CSVPrinter(writer, FORMAT)) {
                for (Transaction transaction : transactions) {
                    printer.printRecord(
                            transaction.getCreatedAt().atZone(ZONE).format(DATE),
                            transaction.getType(),
                            transaction.getCategory(),
                            transaction.getDescription(),
                            transaction.getAmount().toPlainString().replace('.', ','),
                            transaction.getStatus(),
                            transaction.getRejectedReason());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
