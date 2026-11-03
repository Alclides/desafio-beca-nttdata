package com.beca.transactions.adapter.out.account;

import java.math.BigDecimal;

public record AccontBalance(Long UserId, BigDecimal Balance, BigDecimal dailyLimit, BigDecimal perTransactionLimit) {
}
