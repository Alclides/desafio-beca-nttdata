package com.beca.transactions.domain.exception;

public class ResourceAccessDeniedException extends RuntimeException {
    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}
