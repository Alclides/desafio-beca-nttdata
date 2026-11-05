package com.beca.transactions.adapter.out.account;

import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.model.AccountBalance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
;

@RestClientTest(components = MockApiAccountBalanceAdapter.class, properties = "integrations.mockapi-url=http://mockapi.test")
class MockApiTest {
    private static final String URL = "http://mockapi.test/accounts?userId=1";
    private static final String URL_CONTA_1 = "http://mockapi.test/accounts/7";
    // O id do recurso (7) é diferente do userId (1) para o teste provar que o PUT usa o id
    private static final String CONTA_1 = """
            {
                "id": "7",
                "userId": 1,
                "balance": 1000.00,
                "dailyLimit": 500.00,
                "perTransactionLimit": 200.00
            }
            """;

    private static final String CONTA_10 = """
            {"id": "4",
            "userId": 10,
            "balance": 9999,
            "dailyLimit": 9999,
            "perTransactionLimit": 9999}""";

    @Autowired
    private MockApiAccountBalanceAdapter adapter;

    @Autowired
    private MockRestServiceServer server;


    @Test
    void findByUserId_deveMapearOsValoresDaConta() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("[" + CONTA_1 + "]", MediaType.APPLICATION_JSON));

        AccountBalance account = adapter.findByUserId(1L);

        assertThat(account.userId()).isEqualTo(1L);
        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(account.dailyLimit()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(account.perTransactionLimit()).isEqualByComparingTo(new BigDecimal("200"));

    }

    @Test
    void findByUserId_deveEscolherAContaComUserIdExato() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("[" + CONTA_10 + "," + CONTA_1 + "]", MediaType.APPLICATION_JSON));

        assertThat(adapter.findByUserId(1L).balance()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void findByUserId_deveLancarExcecaoQuandoSoVierContaDeOutroUsuario() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("[" + CONTA_10 + "]", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.findByUserId(1L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void findByUserId_deveLancarContaNaoEncontradaQuandoMockApiRetornar404() {
        server.expect(requestTo(URL))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> adapter.findByUserId(1L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void findByUserId_deveLancarExcecaoQuandoMockApiRetornar500() {
        server.expect(requestTo(URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.findByUserId(1L))
                .isInstanceOf(AccountServiceUnavailableException.class);
    }

    private void contaEncontrada(String... contas) {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[" + String.join(",", contas) + "]", MediaType.APPLICATION_JSON));
    }

    @Test
    void adjustBalance_deveFazerPutNoIdDaContaComOSaldoDebitado() {
        contaEncontrada(CONTA_1);
        server.expect(requestTo(URL_CONTA_1))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(899.5))
                .andRespond(withSuccess());

        adapter.adjustBalance(1L, new BigDecimal("-100.50"));

        server.verify();
    }

    @Test
    void adjustBalance_deveSomarOSaldoNoCredito() {
        contaEncontrada(CONTA_1);
        server.expect(requestTo(URL_CONTA_1))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.balance").value(1200.0))
                .andRespond(withSuccess());

        adapter.adjustBalance(1L, new BigDecimal("200"));

        server.verify();
    }

    @Test
    void adjustBalance_deveManterOsOutrosCamposDaConta() {
        contaEncontrada(CONTA_1);
        server.expect(requestTo(URL_CONTA_1))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.dailyLimit").value(500.0))
                .andExpect(jsonPath("$.perTransactionLimit").value(200.0))
                .andRespond(withSuccess());

        adapter.adjustBalance(1L, new BigDecimal("-100"));

        server.verify();
    }

    @Test
    void adjustBalance_deveAtualizarSoAContaComUserIdExato() {
        // O filtro do MockAPI busca por trecho: pedir userId=1 também traz o usuário 10
        contaEncontrada(CONTA_10, CONTA_1);
        server.expect(requestTo(URL_CONTA_1))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        adapter.adjustBalance(1L, new BigDecimal("-100"));

        server.verify();
    }

    @Test
    void adjustBalance_deveLancarExcecaoQuandoOPutRetornar500() {
        contaEncontrada(CONTA_1);
        server.expect(requestTo(URL_CONTA_1))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.adjustBalance(1L, new BigDecimal("-100")))
                .isInstanceOf(AccountServiceUnavailableException.class);
        server.verify();
    }

    // Nos dois testes abaixo só o GET é esperado: um PUT a mais faria o MockRestServiceServer falhar

    @Test
    void adjustBalance_naoDeveFazerPutQuandoAContaNaoExiste() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> adapter.adjustBalance(1L, new BigDecimal("-100")))
                .isInstanceOf(AccountNotFoundException.class);
        server.verify();
    }

    @Test
    void adjustBalance_naoDeveFazerPutQuandoAConsultaFalha() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.adjustBalance(1L, new BigDecimal("-100")))
                .isInstanceOf(AccountServiceUnavailableException.class);
        server.verify();
    }
}

