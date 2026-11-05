package com.beca.transactions.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Erros de login fora do GlobalExceptionHandler, porque acontecem antes de chegar ao controller
@Component
public class SecurityErrorResponder implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        if (exception instanceof AuthenticationServiceException) {
            write(response, HttpStatus.SERVICE_UNAVAILABLE, "Serviço de usuários indisponível, tente novamente mais tarde!");
            return;
        }
        // Sem o cabeçalho WWW-Authenticate: com ele, o navegador tenta o login sozinho, por cima do Authorize do
        // Swagger, e pode falhar com ERR_TOO_MANY_RETRIES. Swagger, Postman e curl já enviam as credenciais direto.
        write(response, HttpStatus.UNAUTHORIZED, "Informe e-mail e senha válidos");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
