package com.beca.transactions.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record ExchangeRate(String currency, BigDecimal rate, LocalDate rateDate) {

    public BigDecimal convert(BigDecimal amountInBrl) {
        return amountInBrl.divide(rate, 2, RoundingMode.HALF_EVEN);
    }
}
