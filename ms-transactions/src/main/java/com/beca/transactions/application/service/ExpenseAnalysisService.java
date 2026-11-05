package com.beca.transactions.application.service;

import com.beca.transactions.application.port.in.AnalyzeExpensesUseCase;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExpenseReport;
import com.beca.transactions.domain.model.Period;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.service.ExpenseCalculator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

public class ExpenseAnalysisService implements AnalyzeExpensesUseCase {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final TransactionRepositoryPort repository;
    private final ExpenseCalculator calculator;

    public ExpenseAnalysisService(TransactionRepositoryPort repository, ExpenseCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Override
    public ExpenseReport<Category> byCategory(Long userId, LocalDate from, LocalDate to) {
        Period period = periodOf(from, to);
        return calculator.byCategory(period, transactionsIn(userId, period), repository.findReversedTransactionIds(userId));
    }

    @Override
    public ExpenseReport<LocalDate> byDay(Long userId, LocalDate from, LocalDate to) {
        Period period = periodOf(from, to);
        return calculator.byDay(period, transactionsIn(userId, period), repository.findReversedTransactionIds(userId));
    }

    @Override
    public ExpenseReport<YearMonth> byMonth(Long userId, LocalDate from, LocalDate to) {
        Period period = periodOf(from, to);
        return calculator.byMonth(period, transactionsIn(userId, period), repository.findReversedTransactionIds(userId));
    }

    private static Period periodOf(LocalDate from, LocalDate to) {
        return Period.resolve(from, to, LocalDate.now(ZONE));
    }

    private List<Transaction> transactionsIn(Long userId, Period period) {
        return repository.findByUser(userId,
                period.from().atStartOfDay(ZONE).toInstant(),
                period.to().plusDays(1).atStartOfDay(ZONE).toInstant(),
                null);
    }
}
