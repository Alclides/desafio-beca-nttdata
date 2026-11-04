package com.beca.transactions.application.service;

import com.beca.transactions.application.port.in.CreateTransactionUseCase;
import com.beca.transactions.application.port.in.FindTransactionUseCase;
import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.exception.InvalidTransactionException;
import com.beca.transactions.domain.exception.TransactionNotFoundException;
import com.beca.transactions.domain.model.AccountBalance;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;
import com.beca.transactions.domain.service.TransactionApprovalPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class TransactionService implements CreateTransactionUseCase, FindTransactionUseCase {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final AccountBalancePort accountBalancePort;
    private final TransactionApprovalPolicy transactionApprovalPolicy;

    public TransactionService(TransactionRepositoryPort transactionRepositoryPort,
                              AccountBalancePort accountBalancePort,
                              TransactionApprovalPolicy transactionApprovalPolicy) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.accountBalancePort = accountBalancePort;
        this.transactionApprovalPolicy = transactionApprovalPolicy;
    }

    @Override
    public Transaction create(CreateTransactionCommand command) {
        validateCorrection(command);

        Transaction tx = Transaction.create(command.userId(), command.type(), command.amount(),
                command.description(), command.category(),
                command.destinationUserId(), command.correctedTransactionId());

        if (tx.getType().isOutgoing()) {
            decide(tx);
        } else {
            tx.approve();
        }
        return transactionRepositoryPort.save(tx);
    }

    @Override
    public Transaction findById(Long transactionId) {
        return transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada: " + transactionId));
    }

    @Override
    public List<Transaction> findByUser(Long userId, LocalDate from, LocalDate to, TransactionType type) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today.withDayOfMonth(today.lengthOfMonth());

        if (start.isAfter(end)) {
            throw new InvalidTransactionException("Data inicial não pode ser depois da data final");
        }

        return transactionRepositoryPort.findByUser(userId, startOf(start), startOf(end.plusDays(1)), type);
    }

    private void validateCorrection(CreateTransactionCommand command) {
        if (command.correctedTransactionId() == null) {
            return;
        }
        Transaction original = findById(command.correctedTransactionId());
        if (!original.getUserId().equals(command.userId())) {
            throw new InvalidTransactionException("Só é possível corrigir transações próprias");
        }
    }

    private void decide(Transaction tx) {
        AccountBalance account;
        try {
            account = accountBalancePort.findByUserId(tx.getUserId());
        } catch (AccountNotFoundException e) {
            tx.reject("ACCOUNT_NOT_FOUND: Conta não encontrada para o usuário");
            return;
        } catch (AccountServiceUnavailableException e) {
            tx.reject("BALANCE_SERVICE_UNAVAILABLE: Serviço de saldo indisponível, tente novamente mais tarde!");
            return;
        }

        LocalDate today = LocalDate.now(ZONE);
        BigDecimal spentToday = transactionRepositoryPort.sumApprovedOutgoing(
                tx.getUserId(), startOf(today), startOf(today.plusDays(1)));

        transactionApprovalPolicy.evaluate(tx, account, spentToday)
                .ifPresentOrElse(tx::reject, tx::approve);
    }

    private static Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }
}
