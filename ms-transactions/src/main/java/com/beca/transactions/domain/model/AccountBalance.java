package com.beca.transactions.domain.model;

import java.math.BigDecimal;

public record AccountBalance(Long userId, BigDecimal balance, BigDecimal dailyLimit, BigDecimal perTransactionLimit) {
}
