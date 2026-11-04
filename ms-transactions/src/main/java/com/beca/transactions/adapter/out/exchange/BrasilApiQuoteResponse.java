package com.beca.transactions.adapter.out.exchange;

import com.beca.transactions.domain.model.ExchangeRate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record BrasilApiQuoteResponse(List<Quote> cotacoes, String moeda, LocalDate data) {

    public record  Quote(
            @JsonProperty("cotacao_venda") BigDecimal sellRate,
            @JsonProperty("tipo_boletim") String bulletinType) {}

    public Optional<ExchangeRate> toDomain() {
        if (cotacoes == null || cotacoes.isEmpty()) {
            return Optional.empty();
        }

        Quote quote = cotacoes.stream()
                .filter(q -> "FECHAMENTO PTAX".equals(q.bulletinType()))
                .findFirst()
                .orElse(cotacoes.get(cotacoes.size() - 1));

        return Optional.of(new ExchangeRate(moeda, quote.sellRate(), data));
    }
}
