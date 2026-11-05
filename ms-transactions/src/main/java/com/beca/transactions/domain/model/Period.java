package com.beca.transactions.domain.model;

import com.beca.transactions.domain.exception.InvalidTransactionException;

import java.time.LocalDate;

public record Period(LocalDate from, LocalDate to) {
    public static Period resolve(LocalDate from, LocalDate to, LocalDate today) {
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today.withDayOfMonth(today.lengthOfMonth());

        if (start.isAfter(end)) {
            throw new InvalidTransactionException("Data inicial não pode ser depois da data final");
        }

        return new Period(start, end);
    }
}
