package com.beca.transactions.application.port.out;

import com.beca.transactions.domain.model.AccountBalance;

import java.math.BigDecimal;

public interface AccountBalancePort {
    AccountBalance findByUserId (Long userId);

    void adjustBalance(Long userId, BigDecimal delta);
}
