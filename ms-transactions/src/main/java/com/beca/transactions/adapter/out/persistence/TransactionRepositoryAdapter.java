package com.beca.transactions.adapter.out.persistence;

import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    // Tipos que contam como saída de dinheiro (todos menos depósito)
    private static final List<TransactionType> OUTGOING_TYPES = Arrays.stream(TransactionType.values())
            .filter(TransactionType::isOutgoing)
            .toList();

    private final SpringDataTransactionRepository repository;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity saved = repository.save(TransactionPersistenceMapper.toEntity(transaction));
        return TransactionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return repository.findById(id).map(TransactionPersistenceMapper::toDomain);
    }

    @Override
    public List<Transaction> findByUser(Long userId, Instant start, Instant end, TransactionType type) {
        List<TransactionEntity> entities = type == null
                ? repository.findByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        userId, start, end)
                : repository.findByUserIdAndTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        userId, type, start, end);

        return entities.stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumApprovedOutgoing(Long userId, Instant start, Instant end) {
        return repository.sumStatusAndTypes(userId, TransactionStatus.APPROVED, OUTGOING_TYPES, start, end);
    }

    @Override
    public boolean hasApprovedCorrection(Long correctedTransactionId) {
        return repository.existsByCorrectedTransactionIdAndStatus(correctedTransactionId, TransactionStatus.APPROVED);
    }
}
