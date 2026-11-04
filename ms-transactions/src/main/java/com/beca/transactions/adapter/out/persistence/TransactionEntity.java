package com.beca.transactions.adapter.out.persistence;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.TransactionStatus;
import com.beca.transactions.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "destination_user_id")
    private Long destinationUserId;

    @Column(name = "corrected_transaction_id")
    private Long correctedTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "usd_rate", precision = 10, scale = 4)
    private BigDecimal usdRate;

    @Column(name = "usd_rate_date")
    private LocalDate usdRateDate;

    @Column(name = "eur_rate", precision = 10, scale = 4)
    private BigDecimal eurRate;

    @Column(name = "eur_rate_date")
    private LocalDate eurRateDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    protected  TransactionEntity() {
    }

    public TransactionEntity(Long id, Long userId, TransactionType type, BigDecimal amount, String description,
                             Category category, Long destinationUserId, Long correctedTransactionId,
                             TransactionStatus status, String rejectedReason, Instant createdAt) {
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
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getDestinationUserId() {
        return destinationUserId;
    }

    public void setDestinationUserId(Long destinationUserId) {
        this.destinationUserId = destinationUserId;
    }

    public Long getCorrectedTransactionId() {
        return correctedTransactionId;
    }

    public void setCorrectedTransactionId(Long correctedTransactionId) {
        this.correctedTransactionId = correctedTransactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public BigDecimal getUsdRate() {
        return usdRate;
    }

    public void setUsdRate(BigDecimal usdRate) {
        this.usdRate = usdRate;
    }

    public LocalDate getUsdRateDate() {
        return usdRateDate;
    }

    public void setUsdRateDate(LocalDate usdRateDate) {
        this.usdRateDate = usdRateDate;
    }

    public BigDecimal getEurRate() {
        return eurRate;
    }

    public void setEurRate(BigDecimal eurRate) {
        this.eurRate = eurRate;
    }

    public LocalDate getEurRateDate() {
        return eurRateDate;
    }

    public void setEurRateDate(LocalDate eurRateDate) {
        this.eurRateDate = eurRateDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEntity that = (TransactionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

