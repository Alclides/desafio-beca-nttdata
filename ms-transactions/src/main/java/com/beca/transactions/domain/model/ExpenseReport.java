package com.beca.transactions.domain.model;

import java.math.BigDecimal;
import java.util.SortedMap;

public record ExpenseReport<K>(Period period, SortedMap<K, BigDecimal> totals, BigDecimal total) {
}
