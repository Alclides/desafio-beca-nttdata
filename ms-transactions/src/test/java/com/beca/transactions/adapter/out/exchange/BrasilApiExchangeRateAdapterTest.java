package com.beca.transactions.adapter.out.exchange;

import com.beca.transactions.domain.exception.ExchangeRateUnavailableException;
import com.beca.transactions.domain.model.ExchangeRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = BrasilApiExchangeRateAdapter.class,
        properties = "integrations.brasilapi-url=http://brasilapi.test")
class BrasilApiExchangeRateAdapterTest {

    private static final String BASE = "http://brasilapi.test/cambio/v1/cotacao/USD/";

    @Autowired
    private BrasilApiExchangeRateAdapter adapter;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findLatest_deveUsarOFechamentoPtax() {
        server.expect(requestTo(BASE + "2026-11-03"))
                .andRespond(withSuccess("""
                        {"cotacoes": [
                          {"cotacao_venda": 5.4898, "tipo_boletim": "ABERTURA"},
                          {"cotacao_venda": 5.4935, "tipo_boletim": "FECHAMENTO PTAX"},
                          {"cotacao_venda": 5.5000, "tipo_boletim": "INTERMEDIÁRIO"}
                        ], "moeda": "USD", "data": "2026-11-03"}
                        """, MediaType.APPLICATION_JSON));

        ExchangeRate rate = adapter.findLatest("USD", LocalDate.of(2026, 11, 3));

        assertThat(rate.currency()).isEqualTo("USD");
        assertThat(rate.rate()).isEqualByComparingTo("5.4935");
        assertThat(rate.rateDate()).isEqualTo(LocalDate.of(2026, 11, 3));
    }

    @Test
    void findLatest_deveUsarOUltimoBoletimSemFechamento() {
        server.expect(requestTo(BASE + "2026-11-03"))
                .andRespond(withSuccess("""
                        {"cotacoes": [
                          {"cotacao_venda": 5.4898, "tipo_boletim": "ABERTURA"},
                          {"cotacao_venda": 5.4915, "tipo_boletim": "INTERMEDIÁRIO"}
                        ], "moeda": "USD", "data": "2026-11-03"}
                        """, MediaType.APPLICATION_JSON));

        ExchangeRate rate = adapter.findLatest("USD", LocalDate.of(2026, 11, 3));

        assertThat(rate.rate()).isEqualByComparingTo("5.4915");
    }

    @Test
    void findLatest_deveDevolverADataRealDaTaxa() {
        // A BrasilAPI responde 200 no sábado, já com a cotação e a data da sexta
        server.expect(requestTo(BASE + "2026-10-31"))
                .andRespond(withSuccess("""
                        {"cotacoes": [
                          {"cotacao_venda": 5.4212, "tipo_boletim": "FECHAMENTO PTAX"}
                        ], "moeda": "USD", "data": "2026-10-30"}
                        """, MediaType.APPLICATION_JSON));

        ExchangeRate rate = adapter.findLatest("USD", LocalDate.of(2026, 10, 31));

        assertThat(rate.rate()).isEqualByComparingTo("5.4212");
        assertThat(rate.rateDate()).isEqualTo(LocalDate.of(2026, 10, 30));
    }

    @Test
    void findLatest_deveRecuarUmDiaQuandoReceber400() {
        // A BrasilAPI responde 400 (NO_TODAY_DATE) quando a data pedida é a de hoje
        server.expect(requestTo(BASE + "2026-11-04"))
                .andRespond(withBadRequest());
        server.expect(requestTo(BASE + "2026-11-03"))
                .andRespond(withSuccess("""
                        {"cotacoes": [
                          {"cotacao_venda": 5.4935, "tipo_boletim": "FECHAMENTO PTAX"}
                        ], "moeda": "USD", "data": "2026-11-03"}
                        """, MediaType.APPLICATION_JSON));

        ExchangeRate rate = adapter.findLatest("USD", LocalDate.of(2026, 11, 4));

        assertThat(rate.rateDate()).isEqualTo(LocalDate.of(2026, 11, 3));
        server.verify();
    }

    @Test
    void findLatest_deveFalharQuandoNaoHouverCotacaoEm5Dias() {
        LocalDate date = LocalDate.of(2026, 11, 4);
        for (int daysBack = 0; daysBack <= 5; daysBack++) {
            server.expect(requestTo(BASE + date.minusDays(daysBack)))
                    .andRespond(withResourceNotFound());
        }

        assertThatThrownBy(() -> adapter.findLatest("USD", date))
                .isInstanceOf(ExchangeRateUnavailableException.class);
        server.verify();
    }

    @Test
    void findLatest_deveFalharSemRepetirQuandoReceber500() {
        server.expect(requestTo(BASE + "2026-11-03"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.findLatest("USD", LocalDate.of(2026, 11, 3)))
                .isInstanceOf(ExchangeRateUnavailableException.class);
        server.verify();
    }
}
