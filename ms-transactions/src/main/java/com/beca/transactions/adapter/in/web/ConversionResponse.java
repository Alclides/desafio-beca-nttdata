package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.ExchangeRate;
import com.beca.transactions.domain.model.ExchangeRates;
import com.beca.transactions.domain.model.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ConversionResponse(
        @Schema(description = "false quando a cotação não pôde ser obtida") boolean available,
        @Schema(description = "Motivo, quando available é false") String reason,
        @Schema(description = "Valor convertido em cada moeda; vazia quando available é false") List<ConvertedAmount> rates
) {

    private static final String UNAVAILABLE_REASON = "Cotação indisponível no momento";

    public record ConvertedAmount(
            @Schema(example = "USD") String currency,
            @Schema(example = "5.4935", description = "Cotação de venda em reais") BigDecimal rate,
            @Schema(example = "2026-11-03", description = "Data real da cotação usada") LocalDate rateDate,
            @Schema(example = "16.36", description = "Valor da transação nessa moeda") BigDecimal convertedAmount
    ) {
        static ConvertedAmount of(ExchangeRate rate, BigDecimal amount) {
            return new ConvertedAmount(rate.currency(), rate.rate(), rate.rateDate(), rate.convert(amount));
        }
    }

    static ConversionResponse from(Transaction transaction) {
        if (!transaction.hasExchangeRates()) {
            return new ConversionResponse(false, UNAVAILABLE_REASON, List.of());
        }
        ExchangeRates rates = transaction.getExchangeRates();
        return new ConversionResponse(true, null, List.of(
                ConvertedAmount.of(rates.usd(), transaction.getAmount()),
                ConvertedAmount.of(rates.eur(), transaction.getAmount())));
    }
}
