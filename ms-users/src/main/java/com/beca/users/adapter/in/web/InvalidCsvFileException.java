package com.beca.users.adapter.in.web;

public class InvalidCsvFileException extends RuntimeException {
    public InvalidCsvFileException(String message) {
        super(message);
    }

    public InvalidCsvFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
