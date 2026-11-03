package com.beca.transactions.domain.service;

import com.beca.transactions.domain.model.AccountBalance;
import com.beca.transactions.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.Optional;

public class TransactionApprovalPolicy {
    public Optional<String> evaluate(Transaction tx, AccountBalance account, BigDecimal spentToday) {
        if (tx.getAmount().compareTo(account.perTransactionLimit()) > 0) {
            return Optional.of("Valor excede o limite por transação");
        }
        if (spentToday.add(tx.getAmount()).compareTo(account.dailyLimit()) > 0) {
            return Optional.of("Valor excede o limite diário");
        }
        if (tx.getAmount().compareTo(account.balance()) > 0) {
            return Optional.of("Saldo insuficiente");
        }

        return Optional.empty();
    }
}
