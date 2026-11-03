package com.beca.transactions.aplication.port.in;

import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface FindTransactionUseCase {
    Transaction findById(Long transactionId);
    List<Transaction> findByUser(Long userId, LocalDate from, LocalDate to, TransactionType type);
}
