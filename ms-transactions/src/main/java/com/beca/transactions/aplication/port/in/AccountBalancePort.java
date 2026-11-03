package com.beca.transactions.aplication.port.in;

import com.beca.transactions.domain.model.AccountBalance;

public interface AccountBalancePort {
    AccountBalance findByUserId (Long userId);
}
