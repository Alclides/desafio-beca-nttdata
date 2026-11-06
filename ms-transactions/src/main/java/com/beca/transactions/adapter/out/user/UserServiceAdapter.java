package com.beca.transactions.adapter.out.user;

import com.beca.transactions.application.port.out.UserAuthenticationPort;
import com.beca.transactions.application.port.out.UserDirectoryPort;
import com.beca.transactions.domain.model.AuthenticatedUser;
import com.beca.transactions.domain.exception.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

@Component
public class UserServiceAdapter implements UserDirectoryPort, UserAuthenticationPort {

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

    @Override
    public Optional<AuthenticatedUser> authenticate(String email, String password) {
        try {
            AuthenticatedUser user = restClient.post()
                    .uri("/users/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "password", password))
                    .retrieve()
                    .body(AuthenticatedUser.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            return Optional.empty();
        } catch (RestClientException | IllegalArgumentException e) {
            throw new UserServiceUnavailableException("Falha ao autenticar no serviço de usuários: " + e.getMessage());
        }
    }
}
