package com.beca.transactions.adapter.out.user;

import com.beca.transactions.application.port.out.UserDirectoryPort;
import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceAdapter implements UserDirectoryPort {

    private final RestClient restClient;

    public UserServiceAdapter(RestClient.Builder builder, @Value("${integrations.users-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean exists(Long userId) {
        try {
            restClient.get().uri("/users/{id}", userId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException | IllegalArgumentException e) {
            throw new UserServiceUnavailableException("Falha ao consultar o serviço de usuários: " + e.getMessage());
        }
    }
}
