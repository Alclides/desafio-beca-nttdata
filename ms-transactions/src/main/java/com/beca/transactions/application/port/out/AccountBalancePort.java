package com.beca.transactions.application.port.out;

import com.beca.transactions.domain.model.AccountBalance;

public interface AccountBalancePort {
    AccountBalance findByUserId (Long userId);
}
