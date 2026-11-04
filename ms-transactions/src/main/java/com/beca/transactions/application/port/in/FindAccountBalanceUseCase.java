package com.beca.transactions.application.port.in;

import com.beca.transactions.domain.model.AccountBalance;

public interface FindAccountBalanceUseCase {
    AccountBalance findByUserId(Long userId);
}
