package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        @Schema(example = "4") Long id,
        @Schema(example = "1") Long userId,
        @Schema(example = "PURCHASE") TransactionType type,
        @Schema(example = "89.90") BigDecimal amount,
        @Schema(example = "Mercado do mês") String description,
        @Schema(example = "ALIMENTACAO") Category category,
        @Schema(description = "Preenchido só em transferência") Long destinationUserId,
        @Schema(description = "Preenchido quando esta transação corrige outra") Long correctedTransactionId,
        @Schema(example = "APPROVED") TransactionStatus status,
        @Schema(description = "Motivo, quando o status é REJECTED") String rejectedReason,
        @Schema(example = "2026-11-03T18:00:00Z") Instant createdAt
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
