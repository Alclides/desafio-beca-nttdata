package com.beca.transactions.domain.model;

import java.math.BigDecimal;

public record AccountBalance(Long UserId, BigDecimal Balance, BigDecimal dailyLimit, BigDecimal perTransactionLimit) {
}
