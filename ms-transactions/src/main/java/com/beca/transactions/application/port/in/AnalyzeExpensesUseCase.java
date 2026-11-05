package com.beca.transactions.application.port.in;

import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.ExpenseReport;

import java.time.LocalDate;
import java.time.YearMonth;

public interface AnalyzeExpensesUseCase {
    ExpenseReport<Category> byCategory(Long userId, LocalDate from, LocalDate to);

    ExpenseReport<LocalDate> byDay(Long userId, LocalDate from, LocalDate to);

    ExpenseReport<YearMonth> byMonth(Long userId, LocalDate from, LocalDate to);
}
