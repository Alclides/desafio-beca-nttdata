package com.beca.transactions.domain.model;

import com.beca.transactions.domain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Transaction {

    private final Long id;
    private final Long userId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String description;
    private final Category category;
    private final Long destinationUserId;
    private final Long correctedTransactionId;
    private TransactionStatus status;
    private String rejectedReason;
    private final Instant createdAt;
    private ExchangeRates exchangeRates;


    public Transaction(Long id, Long userId, TransactionType type, BigDecimal amount, String description,
                       Category category, Long destinationUserId, Long correctedTransactionId,
                       TransactionStatus status, String rejectedReason, ExchangeRates exchangeRates , Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.destinationUserId = destinationUserId;
        this.correctedTransactionId = correctedTransactionId;
        this.status = status;
        this.rejectedReason = rejectedReason;
        this.exchangeRates = exchangeRates;
        this.createdAt = createdAt;

    }

    public static Transaction create(Long userId, TransactionType type, BigDecimal amount, String description,
                                     Category category, Long destinationUserId, Long correctedTransactionId) {
        if (userId == null)
            throw new InvalidTransactionException("Usuário é obrigatório");
        if (type == null)
            throw new InvalidTransactionException("Tipo da transação é obrigatório");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidTransactionException("Valor deve ser maior que zero");
        if (type == TransactionType.TRANSFER && destinationUserId == null)
            throw new InvalidTransactionException("Transferência exige usuário de destino");
        if (type == TransactionType.TRANSFER && destinationUserId.equals(userId))
            throw new InvalidTransactionException("Não é possível transferir para si mesmo");
        if (type != TransactionType.TRANSFER && destinationUserId != null)
            throw new InvalidTransactionException("Usuário de destino só é permitido em transferência");
        if (type == TransactionType.PURCHASE && category == null)
            throw new InvalidTransactionException("Compra exige categoria");

        Category finalCategory = category != null ? category : Category.OUTROS;
        return new Transaction(null, userId, type, amount, description, finalCategory,
                destinationUserId, correctedTransactionId, null, null, null, Instant.now());
    }

    public void approve() {
        ensureNotDecided();
        this.status = TransactionStatus.APPROVED;
    }

    public void reject(String reason) {
        ensureNotDecided();
        this.status = TransactionStatus.REJECTED;
        this.rejectedReason = reason;
    }

    private void ensureNotDecided() {
        if (status != null) {
            throw new IllegalStateException("Transação já foi decidida: " + status);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Long getDestinationUserId() {
        return destinationUserId;
    }

    public Long getCorrectedTransactionId() {
        return correctedTransactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ExchangeRates getExchangeRates() { return exchangeRates; }

    public boolean hasExchangeRates() {
        return exchangeRates != null;
    }

    public void applyExchangeRates(ExchangeRates exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
