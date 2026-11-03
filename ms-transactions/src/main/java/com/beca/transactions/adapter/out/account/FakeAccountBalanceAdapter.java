package com.beca.transactions.adapter.out.account;

import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.domain.model.AccountBalance;

import java.math.BigDecimal;

public class FakeAccountBalanceAdapter  implements AccountBalancePort {
    @Override
    public AccountBalance findByUserId(Long userId) {
        return new AccountBalance(userId, new BigDecimal("1000"),
                new BigDecimal("2000"), new BigDecimal("500"));
    }
}
