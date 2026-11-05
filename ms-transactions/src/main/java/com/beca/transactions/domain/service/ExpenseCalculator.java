package com.beca.transactions.domain.service;

import com.beca.transactions.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExpenseCalculator {

    private final ZoneId zone;

    public ExpenseCalculator(ZoneId zone) {
        this.zone = zone;
    }

    public ExpenseReport<Category> byCategory(Period period, List<Transaction> transactions, Set<Long> reversedIds) {
        return report(period, transactions, reversedIds, Transaction::getCategory);
    }

    public ExpenseReport<LocalDate> byDay(Period period, List<Transaction> transactions, Set<Long> reversedIds) {
        return report(period, transactions, reversedIds, this::dateOf);
    }

    public ExpenseReport<YearMonth> byMonth(Period period, List<Transaction> transactions, Set<Long> reversedIds) {
        return report(period, transactions, reversedIds, tx -> YearMonth.from(dateOf(tx)));
    }

    private <K extends Comparable<? super K>> ExpenseReport<K> report(Period period, List<Transaction> transactions,
                                                                      Set<Long> reversedIds, Function<Transaction, K> key) {
        SortedMap<K, BigDecimal> totals = transactions.stream()
                .filter(tx -> isExpense(tx, reversedIds))
                .collect(Collectors.groupingBy(key, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        BigDecimal total = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ExpenseReport<>(period, totals, total);
    }

    private boolean isExpense(Transaction tx, Set<Long> reversedIds) {
        return tx.getStatus() == TransactionStatus.APPROVED
                && tx.getType().isOutgoing()
                && tx.getCorrectedTransactionId() == null
                && !reversedIds.contains(tx.getId());
    }

    private LocalDate dateOf(Transaction tx) {
        return tx.getCreatedAt().atZone(zone).toLocalDate();
    }
}
