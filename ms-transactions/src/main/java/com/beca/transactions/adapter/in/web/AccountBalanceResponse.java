package com.beca.transactions.adapter.in.web;

import com.beca.transactions.domain.model.AccountBalance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        @Schema(example = "1") Long userId,
        @Schema(example = "1000.00") BigDecimal balance,
        @Schema(example = "2000.00") BigDecimal dailyLimit,
        @Schema(example = "500.00") BigDecimal perTransactionLimit
) {

    static AccountBalanceResponse from(AccountBalance account) {
        return new AccountBalanceResponse(
                account.userId(),
                account.balance(),
                account.dailyLimit(),
                account.perTransactionLimit()
        );
    }
}
