package com.beca.users.adapter.in.web;

import com.beca.users.adapter.in.web.UserImportResponse.LineError;
import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsvUserImporterTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private CreateUserUseCase createUserUseCase;
    private CsvUserImporter importer;

    @BeforeEach
    void setUp() {
        createUserUseCase = mock(CreateUserUseCase.class);
        importer = new CsvUserImporter(createUserUseCase, VALIDATOR_FACTORY.getValidator());
    }

    @AfterAll
    static void tearDown() {
        VALIDATOR_FACTORY.close();
    }

    private static InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importUsers_deveCriarTodosOsUsuariosDeUmCsvValido() {
        UserImportResponse result = importer.importUsers(csv("""
                name,email,password
                Maria,maria@teste.com,123456
                João,joao@teste.com,654321
                """));

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.successes()).isEqualTo(2);
        assertThat(result.failures()).isZero();
        assertThat(result.errors()).isEmpty();
        verify(createUserUseCase).create(new CreateUserCommand("Maria", "maria@teste.com", "123456"));
        verify(createUserUseCase).create(new CreateUserCommand("João", "joao@teste.com", "654321"));
    }

    @Test
    void importUsers_deveReportarLinhasInvalidasEContinuarImportacao() {
        UserImportResponse result = importer.importUsers(csv("""
                name,email,password
                ,sem.nome@teste.com,123456
                Maria,email-invalido,123456
                João,joao@teste.com,123
                Ana,ana@teste.com,123456
                """));

        assertThat(result.total()).isEqualTo(4);
        assertThat(result.successes()).isEqualTo(1);
        assertThat(result.failures()).isEqualTo(3);
        assertThat(result.errors()).extracting(LineError::line).containsExactly(2L, 3L, 4L);
        assertThat(result.errors()).extracting(LineError::email)
                .containsExactly("sem.nome@teste.com", "email-invalido", "joao@teste.com");
        assertThat(result.errors().get(0).reason()).startsWith("name:");
        assertThat(result.errors().get(1).reason()).startsWith("email:");
        assertThat(result.errors().get(2).reason()).startsWith("password:");
        verify(createUserUseCase, times(1)).create(any());
        verify(createUserUseCase).create(new CreateUserCommand("Ana", "ana@teste.com", "123456"));
    }

    @Test
    void importUsers_deveReportarEmailDuplicadoSemInterromper() {
        when(createUserUseCase.create(new CreateUserCommand("Maria", "maria@teste.com", "123456")))
                .thenThrow(new EmailAlreadyExistsException("E-mail já cadastrado: maria@teste.com"));

        UserImportResponse result = importer.importUsers(csv("""
                name,email,password
                Maria,maria@teste.com,123456
                João,joao@teste.com,123456
                """));

        assertThat(result.successes()).isEqualTo(1);
        assertThat(result.errors()).containsExactly(
                new LineError(2, "maria@teste.com", "E-mail já cadastrado: maria@teste.com"));
        verify(createUserUseCase).create(new CreateUserCommand("João", "joao@teste.com", "123456"));
    }

    @Test
    void importUsers_deveReportarLinhaComColunasFaltando() {
        UserImportResponse result = importer.importUsers(csv("""
                name,email,password
                Maria,maria@teste.com
                """));

        assertThat(result.failures()).isEqualTo(1);
        assertThat(result.errors().get(0).reason()).startsWith("password:");
        verify(createUserUseCase, never()).create(any());
    }

    @Test
    void importUsers_deveAceitarBomCabecalhoEmOutraOrdemEEspacos() {
        UserImportResponse result = importer.importUsers(csv("""
                ﻿Email, Password ,NAME

                 maria@teste.com , 123456 , Maria\s
                """));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.successes()).isEqualTo(1);
        verify(createUserUseCase).create(new CreateUserCommand("Maria", "maria@teste.com", "123456"));
    }

    @Test
    void importUsers_deveAceitarUltimaLinhaSemQuebraDeLinha() {
        UserImportResponse result = importer.importUsers(csv(
                "name,email,password\nMaria,maria@teste.com,123456\nJoão,email-invalido,123456"));

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.errors()).extracting(LineError::line).containsExactly(3L);
    }

    @Test
    void importUsers_deveRetornarResumoZeradoQuandoSoTemCabecalho() {
        UserImportResponse result = importer.importUsers(csv("name,email,password\n"));

        assertThat(result.total()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void importUsers_deveRejeitarCabecalhoSemColunasObrigatorias() {
        assertThatThrownBy(() -> importer.importUsers(csv("nome,email\nMaria,maria@teste.com\n")))
                .isInstanceOf(InvalidCsvFileException.class)
                .hasMessageContaining("name")
                .hasMessageContaining("password");

        verify(createUserUseCase, never()).create(any());
    }

    @Test
    void importUsers_deveRejeitarArquivoVazio() {
        assertThatThrownBy(() -> importer.importUsers(csv("")))
                .isInstanceOf(InvalidCsvFileException.class);
    }

    @Test
    void importUsers_deveRejeitarCsvMalformado() {
        assertThatThrownBy(() -> importer.importUsers(csv("name,email,password\n\"Maria\"x,maria@teste.com,123456\n")))
                .isInstanceOf(InvalidCsvFileException.class);
    }

    @Test
    void csvDeExemploDoRepositorio_deveSerTodoValido() throws Exception {
        try (InputStream sample = Files.newInputStream(Path.of("../samples/usuarios.csv"))) {
            UserImportResponse result = importer.importUsers(sample);

            assertThat(result.total()).isEqualTo(5);
            assertThat(result.failures()).isZero();
        }
    }
}
