package com.beca.transactions.adapter.out.exchange;

import com.beca.transactions.application.port.out.ExchangeRatePort;
import com.beca.transactions.domain.exception.ExchangeRateUnavailableException;
import com.beca.transactions.domain.model.ExchangeRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class BrasilApiExchangeRateAdapter implements ExchangeRatePort {

    private static final int MAX_DAYS_BACK = 5;

    private final RestClient restClient;

    public BrasilApiExchangeRateAdapter(RestClient.Builder builder, @Value("${integrations.brasilapi-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public ExchangeRate findLatest(String currency, LocalDate date) {
        for (int daysBack = 0; daysBack <= MAX_DAYS_BACK; daysBack++) {
            LocalDate day = date.minusDays(daysBack);
            try {
                BrasilApiQuoteResponse response = restClient.get()
                        .uri("/cambio/v1/cotacao/{moeda}/{data}", currency, day)
                        .retrieve()
                        .body(BrasilApiQuoteResponse.class);

                if (response != null) {
                    Optional<ExchangeRate> rate = response.toDomain();
                    if (rate.isPresent()) {
                        return rate.get();
                    }
                }
            } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound e) {
                // Sem cotação nesse dia (a API responde 400 para a data de hoje): tenta o dia anterior
            } catch (RestClientException | IllegalArgumentException e) {
                throw new ExchangeRateUnavailableException("Falha ao consultar câmbio: " + e.getMessage());
            }
        }
        throw new ExchangeRateUnavailableException("Sem cotação de " + currency + " nos últimos " + MAX_DAYS_BACK + " dias");
    }
}
