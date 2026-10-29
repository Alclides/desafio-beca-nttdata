package com.beca.users.adapter.in.web;

import com.beca.users.adapter.in.web.UserImportResponse.LineError;
import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lê um CSV com cabeçalho name,email,password e cria um usuário por linha.
 * Linhas inválidas ou com e-mail duplicado são reportadas sem interromper a importação.
 */
@Component
public class CsvUserImporter {

    static final List<String> REQUIRED_COLUMNS = List.of("name", "email", "password");

    private static final char BOM = '﻿';

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    private final CreateUserUseCase createUserUseCase;
    private final Validator validator;

    public CsvUserImporter(CreateUserUseCase createUserUseCase, Validator validator) {
        this.createUserUseCase = createUserUseCase;
        this.validator = validator;
    }

    public UserImportResponse importUsers(InputStream input) {
        try (CSVParser parser = FORMAT.parse(openReader(input))) {
            validateHeader(parser.getHeaderNames());

            int total = 0;
            List<LineError> errors = new ArrayList<>();
            for (CSVRecord record : parser) {
                total++;
                long line = parser.getCurrentLineNumber();
                CreateUserRequest request = new CreateUserRequest(
                        value(record, "name"), value(record, "email"), value(record, "password"));

                String reason = importLine(request);
                if (reason != null) {
                    errors.add(new LineError(line, request.email(), reason));
                }
            }
            return new UserImportResponse(total, total - errors.size(), errors.size(), errors);
        } catch (IOException ex) {
            throw new InvalidCsvFileException("Não foi possível ler o arquivo CSV", ex);
        } catch (UncheckedIOException ex) {
            throw new InvalidCsvFileException("Arquivo CSV inválido: " + ex.getCause().getMessage(), ex);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new InvalidCsvFileException("Arquivo CSV inválido: " + ex.getMessage(), ex);
        }
    }

    /** Retorna o motivo da falha, ou null se o usuário foi criado. */
    private String importLine(CreateUserRequest request) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return violations.stream()
                    .sorted(Comparator.comparing(v -> REQUIRED_COLUMNS.indexOf(v.getPropertyPath().toString())))
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
        }

        try {
            createUserUseCase.create(new CreateUserCommand(request.name(), request.email(), request.password()));
            return null;
        } catch (EmailAlreadyExistsException ex) {
            return ex.getMessage();
        }
    }

    private static BufferedReader openReader(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        // Excel salva CSV em UTF-8 com BOM, que grudaria no nome da primeira coluna
        reader.mark(1);
        if (reader.read() != BOM) {
            reader.reset();
        }
        return reader;
    }

    private static void validateHeader(List<String> header) {
        if (header.isEmpty()) {
            throw new InvalidCsvFileException("Arquivo CSV vazio");
        }
        List<String> normalized = header.stream().map(String::toLowerCase).toList();
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(column -> !normalized.contains(column))
                .toList();
        if (!missing.isEmpty()) {
            throw new InvalidCsvFileException("Colunas obrigatórias ausentes no cabeçalho: " + String.join(", ", missing)
                    + ". Esperado: " + String.join(",", REQUIRED_COLUMNS));
        }
    }

    private static String value(CSVRecord record, String column) {
        return record.isSet(column) ? record.get(column) : null;
    }
}
