package com.beca.transactions.application.port.out;

import com.beca.transactions.domain.model.ExchangeRate;

import java.time.LocalDate;

public interface ExchangeRatePort {

    ExchangeRate findLatest(String currency, LocalDate date);
}
