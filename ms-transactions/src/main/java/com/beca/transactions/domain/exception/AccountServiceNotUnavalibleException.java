package com.beca.transactions.domain.exception;

public class AccountServiceNotUnavalibleException extends RuntimeException {
    public AccountServiceNotUnavalibleException(String message) {
        super(message);
    }
}
