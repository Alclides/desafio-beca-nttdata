package com.beca.transactions.application.service;

import com.beca.transactions.application.port.in.FindAccountBalanceUseCase;
import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.domain.model.AccountBalance;

public class AccountBalanceService implements FindAccountBalanceUseCase {

    private final AccountBalancePort accountBalancePort;

    public AccountBalanceService(AccountBalancePort accountBalancePort) {
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    public AccountBalance findByUserId(Long userId) {
        return accountBalancePort.findByUserId(userId);
    }
}
