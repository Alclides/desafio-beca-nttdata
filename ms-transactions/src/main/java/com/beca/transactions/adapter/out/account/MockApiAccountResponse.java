package com.beca.transactions.adapter.out.account;

import com.beca.transactions.domain.model.AccountBalance;

import java.math.BigDecimal;

public record MockApiAccountResponse(String id, Long userId, BigDecimal balance, BigDecimal dailyLimit, BigDecimal perTransactionLimit) {

    public AccountBalance toDomain()
    {
        return new AccountBalance(userId,balance,dailyLimit,perTransactionLimit);
    }

    MockApiAccountResponse withBalance(BigDecimal newBalance) {
        return new MockApiAccountResponse(id, userId, newBalance, dailyLimit, perTransactionLimit);
    }
}
