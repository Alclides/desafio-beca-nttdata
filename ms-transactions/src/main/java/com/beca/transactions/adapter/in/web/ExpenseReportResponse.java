package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.ExpenseReport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseReportResponse<K>(
        @Schema(example = "2026-11-01", description = "Início do período usado") LocalDate from,
        @Schema(example = "2026-11-30", description = "Fim do período usado") LocalDate to,
        @Schema(description = "Um total por categoria, dia ou mês; só aparece o que teve despesa") List<Line<K>> lines,
        @Schema(example = "180.00", description = "Soma de todas as linhas") BigDecimal total
) {

    public record Line<K>(
            @Schema(description = "A categoria, o dia (AAAA-MM-DD) ou o mês (AAAA-MM)") K key,
            @Schema(example = "150.00") BigDecimal total
    ) {
    }

    static <K> ExpenseReportResponse<K> from(ExpenseReport<K> report) {
        List<Line<K>> lines = report.totals().entrySet().stream()
                .map(entry -> new Line<>(entry.getKey(), entry.getValue()))
                .toList();
        return new ExpenseReportResponse<>(report.period().from(), report.period().to(), lines, report.total());
    }
}
