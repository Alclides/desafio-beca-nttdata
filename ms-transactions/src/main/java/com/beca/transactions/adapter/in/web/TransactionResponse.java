package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long userId,
        TransactionType type,
        BigDecimal amount,
        String description,
        Category category,
        Long destinationUserId,
        Long correctedTransactionId,
        TransactionStatus status,
        String rejectedReason,
        Instant createdAt
) {

    static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getDestinationUserId(),
                transaction.getCorrectedTransactionId(),
                transaction.getStatus(),
                transaction.getRejectedReason(),
                transaction.getCreatedAt()
        );
    }
}
