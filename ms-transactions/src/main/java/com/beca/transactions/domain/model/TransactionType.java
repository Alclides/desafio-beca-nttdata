package com.beca.transactions.domain.model;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    PURCHASE;


    public boolean isOutgoing() {
        return this != DEPOSIT;
    }
}
