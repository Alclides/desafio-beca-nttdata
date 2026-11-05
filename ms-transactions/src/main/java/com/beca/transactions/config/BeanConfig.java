package com.beca.transactions.config;

import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.application.port.out.ExchangeRatePort;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.application.port.out.UserDirectoryPort;
import com.beca.transactions.application.service.AccountBalanceService;
import com.beca.transactions.application.service.ExpenseAnalysisService;
import com.beca.transactions.application.service.TransactionService;
import com.beca.transactions.domain.service.ExpenseCalculator;
import com.beca.transactions.domain.service.TransactionApprovalPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class BeanConfig {

    @Bean
    public TransactionApprovalPolicy transactionApprovalPolicy() {
        return new TransactionApprovalPolicy();
    }

    @Bean
    public TransactionService transactionService(TransactionRepositoryPort transactionRepository, AccountBalancePort accountBalancePort, TransactionApprovalPolicy transactionApprovalPolicy, ExchangeRatePort exchangeRatePort, UserDirectoryPort userDirectoryPort) {

        return new TransactionService(transactionRepository, accountBalancePort, transactionApprovalPolicy, exchangeRatePort, userDirectoryPort);

    }

    @Bean
    public AccountBalanceService accountBalanceService(AccountBalancePort accountBalancePort) {
        return new AccountBalanceService(accountBalancePort);
    }

    @Bean
    public ExpenseCalculator expenseCalculator() {
        return new ExpenseCalculator(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public ExpenseAnalysisService expenseAnalysisService(TransactionRepositoryPort transactionRepository, ExpenseCalculator expenseCalculator) {
        return new ExpenseAnalysisService(transactionRepository, expenseCalculator);
    }

}
