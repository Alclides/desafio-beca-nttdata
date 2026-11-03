package com.beca.transactions.adapter.out.persistence;

import com.beca.transactions.domain.model.Transaction;

final class TransactionPersistenceMapper {

    private TransactionPersistenceMapper() {
    }

    static TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
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

    static Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getDestinationUserId(),
                entity.getCorrectedTransactionId(),
                entity.getStatus(),
                entity.getRejectedReason(),
                entity.getCreatedAt()
        );
    }
}
