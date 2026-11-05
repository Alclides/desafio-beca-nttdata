package com.beca.transactions.adapter.out.account;

import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.model.AccountBalance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class MockApiAccountBalanceAdapter implements AccountBalancePort {

    private final RestClient restClient;

    public MockApiAccountBalanceAdapter(RestClient.Builder builder, @Value("${integrations.mockapi-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    private MockApiAccountResponse findAccount(Long userId) {
        try {
            MockApiAccountResponse[] accounts = restClient.get()
                    .uri("/accounts?userId={userId}", userId)
                    .retrieve()
                    .body(MockApiAccountResponse[].class);

            return Arrays.stream(accounts)
                    .filter(account -> userId.equals(account.userId()))
                    .findFirst()
                    .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada para o usuário: " + userId));
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException("Conta não encontrada para o usuário: " + userId);
        } catch (RestClientException | IllegalArgumentException e) {
            throw new AccountServiceUnavailableException("Falha ao consultar serviço de saldo: " + e.getMessage());
        }
    }


    @Override
    public AccountBalance findByUserId(Long userId) {
       return findAccount(userId).toDomain();
    }

    @Override
    public void adjustBalance(Long userId, BigDecimal delta) {
        MockApiAccountResponse account = findAccount(userId);
        try {
            restClient.put()
                    .uri("/accounts/{id}", account.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(account.withBalance(account.balance().add(delta)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new AccountServiceUnavailableException("Falha ao atualizar saldo: " + e.getMessage());
        }
}}
