package com.beca.transactions.application.port.out;

import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);

    Optional<Transaction> findById(Long userId);

    List<Transaction> findByUser(Long userId, Instant start, Instant end, TransactionType type);

    BigDecimal sumApprovedOutgoing(Long userId, Instant start, Instant end);

    boolean hasApprovedCorrection(Long correctedTransactionId);

    // Sem filtro de data: uma compra de outubro estornada em novembro não pode contar no resumo de outubro
    Set<Long> findReversedTransactionIds(Long userId);
}
