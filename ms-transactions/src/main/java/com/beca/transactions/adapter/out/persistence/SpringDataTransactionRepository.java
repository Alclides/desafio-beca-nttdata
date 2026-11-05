package com.beca.transactions.adapter.out.persistence;

import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc (
            Long userId, Instant start, Instant end
    );

    List<TransactionEntity> findByUserIdAndTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long userId, TransactionType type, Instant start, Instant end
    );
    @Query("""
    select coalesce(sum(t.amount), 0) from TransactionEntity t
    where t.userId = :userId
    and t.status = :status
    and t.type in :types
    and t.createdAt >= :start and t.createdAt < :end
    """)
    BigDecimal sumStatusAndTypes(@Param("userId") Long userId,
                                 @Param("status") TransactionStatus status,
                                 @Param("types") Collection<TransactionType> types,
                                 @Param("start") Instant start,
                                 @Param("end") Instant end);

    boolean existsByCorrectedTransactionIdAndStatus(Long correctedTransactionId, TransactionStatus status);
}
