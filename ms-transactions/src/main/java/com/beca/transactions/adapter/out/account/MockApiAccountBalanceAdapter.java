package com.beca.transactions.adapter.out.account;

import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.model.AccountBalance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;

@Component
public class MockApiAccountBalanceAdapter implements AccountBalancePort {

    private final RestClient restClient;

    public MockApiAccountBalanceAdapter(RestClient.Builder builder, @Value("${integrations.mockapi-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }


    @Override
    public AccountBalance findByUserId(Long userId) {
        try {
            MockApiAccountResponse[] accounts = restClient.get()
                    .uri("/accounts?userId={userId}", userId)
                    .retrieve()
                    .body(MockApiAccountResponse[].class);

            return Arrays.stream(accounts)
                    .filter(account -> userId.equals(account.userId()))
                    .findFirst()
                    .map(MockApiAccountResponse::toDomain)
                    .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada para o usuário: " + userId));
        } catch (HttpClientErrorException.NotFound e) {
            // O MockAPI responde 404 quando o filtro por userId não encontra nenhuma conta
            throw new AccountNotFoundException("Conta não encontrada para o usuário: " + userId);
        } catch (RestClientException | IllegalArgumentException e) {
            throw new AccountServiceUnavailableException("Falha ao consultar serviço de saldo: " + e.getMessage());
        }
    }
}
