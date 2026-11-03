package com.beca.transactions.application.port.in;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;

import java.math.BigDecimal;

public interface CreateTransactionUseCase {

    Transaction create(CreateTransactionCommand command);

    record CreateTransactionCommand(Long userId, TransactionType type, BigDecimal amount, String description, Category category, Long destinationUserId, Long correctedTransactionId) {}

}
