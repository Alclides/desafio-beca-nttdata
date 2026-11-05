package com.beca.transactions.config;

import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.application.port.out.ExchangeRatePort;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.application.service.AccountBalanceService;
import com.beca.transactions.application.service.TransactionService;
import com.beca.transactions.domain.service.TransactionApprovalPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public TransactionApprovalPolicy transactionApprovalPolicy() {
        return new TransactionApprovalPolicy();
    }

    @Bean
    public TransactionService transactionService(TransactionRepositoryPort transactionRepository, AccountBalancePort accountBalancePort, TransactionApprovalPolicy transactionApprovalPolicy, ExchangeRatePort exchangeRatePort) {

        return new TransactionService(transactionRepository, accountBalancePort, transactionApprovalPolicy, exchangeRatePort);

    }

    @Bean
    public AccountBalanceService accountBalanceService(AccountBalancePort accountBalancePort) {
        return new AccountBalanceService(accountBalancePort);
    }


}
