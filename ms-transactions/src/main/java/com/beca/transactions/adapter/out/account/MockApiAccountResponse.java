package com.beca.transactions.adapter.out.account;

import com.beca.transactions.domain.model.AccountBalance;

import java.math.BigDecimal;

public record MockApiAccountResponse(Long userId, BigDecimal balance, BigDecimal dailyLimit, BigDecimal perTransactionLimit) {

    public AccountBalance toDomain()
    {
        return new AccountBalance(userId,balance,dailyLimit,perTransactionLimit);
    }
}
