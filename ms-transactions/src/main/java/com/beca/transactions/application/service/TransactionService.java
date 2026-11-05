package com.beca.transactions.application.service;

import com.beca.transactions.application.port.in.CreateTransactionUseCase;
import com.beca.transactions.application.port.in.FindTransactionUseCase;
import com.beca.transactions.application.port.out.AccountBalancePort;
import com.beca.transactions.application.port.out.ExchangeRatePort;
import com.beca.transactions.application.port.out.TransactionRepositoryPort;
import com.beca.transactions.domain.exception.AccountNotFoundException;
import com.beca.transactions.domain.exception.AccountServiceUnavailableException;
import com.beca.transactions.domain.exception.ExchangeRateUnavailableException;
import com.beca.transactions.domain.exception.InvalidTransactionException;
import com.beca.transactions.domain.exception.TransactionNotFoundException;
import com.beca.transactions.domain.model.*;
import com.beca.transactions.domain.service.TransactionApprovalPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public class TransactionService implements CreateTransactionUseCase, FindTransactionUseCase {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final AccountBalancePort accountBalancePort;
    private final TransactionApprovalPolicy transactionApprovalPolicy;
    private final ExchangeRatePort exchangeRatePort;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepositoryPort transactionRepositoryPort,
                              AccountBalancePort accountBalancePort,
                              TransactionApprovalPolicy transactionApprovalPolicy, ExchangeRatePort exchangeRatePort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.accountBalancePort = accountBalancePort;
        this.transactionApprovalPolicy = transactionApprovalPolicy;
        this.exchangeRatePort = exchangeRatePort;
    }

    @Override
    public Transaction create(CreateTransactionCommand command) {
        Transaction original = validateCorrection(command);

        Transaction tx = Transaction.create(command.userId(), command.type(), command.amount(),
                command.description(), command.category(),
                command.destinationUserId(), command.correctedTransactionId());

        if (tx.getType().isOutgoing()) {
            decide(tx);
        } else {
            reversalBlocker(original).ifPresentOrElse(tx::reject, tx::approve);
        }
        tryApplyExchangeRates(tx);
        Transaction saved = transactionRepositoryPort.save(tx);
        updateBalances(saved, original);
        return saved;
    }

    @Override
    public Transaction findById(Long transactionId) {
        Transaction tx = loadOrThrow(transactionId);
        if (!tx.hasExchangeRates() && tryApplyExchangeRates(tx)) {
            return transactionRepositoryPort.save(tx);
        }
        return tx;
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

    private Transaction loadOrThrow(Long transactionId) {
        return transactionRepositoryPort.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada: " + transactionId));
    }

    private boolean tryApplyExchangeRates(Transaction tx) {
        LocalDate date = tx.getCreatedAt().atZone(ZONE).toLocalDate();
        try {
            ExchangeRate usd = exchangeRatePort.findLatest("USD", date);
            ExchangeRate eur = exchangeRatePort.findLatest("EUR", date);
            tx.applyExchangeRates(new ExchangeRates(usd, eur));
            return true;
        } catch (ExchangeRateUnavailableException e) {
            return false;
        }
    }

    private Transaction validateCorrection(CreateTransactionCommand command) {
        if (command.correctedTransactionId() == null) {
            return null;
        }
        Transaction original = loadOrThrow(command.correctedTransactionId());
        if (!original.getUserId().equals(command.userId())) {
            throw new InvalidTransactionException("Só é possível corrigir transações próprias");
        }
        if (original.getStatus() != TransactionStatus.APPROVED) {
            throw new InvalidTransactionException("Só é possível corrigir transações aprovadas");
        }
        if (original.getCorrectedTransactionId() != null) {
            throw new InvalidTransactionException("Não é possível estornar um estorno");
        }
        if (transactionRepositoryPort.hasApprovedCorrection(original.getId())) {
            throw new InvalidTransactionException("Esta transação já foi estornada");
        }
        if (command.amount() == null || command.amount().compareTo(original.getAmount()) != 0) {
            throw new InvalidTransactionException("O estorno deve ter o mesmo valor da transação original");
        }
        TransactionType expected = original.getType().isOutgoing() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
        if (command.type() != expected) {
            throw new InvalidTransactionException("O estorno desta transação deve ser do tipo " + expected);
        }
        return original;
    }

    // Estornar uma transferência tira o dinheiro do destino: sem saldo confirmado, o estorno não é aprovado
    private Optional<String> reversalBlocker(Transaction original) {
        if (original == null || original.getType() != TransactionType.TRANSFER) {
            return Optional.empty();
        }
        try {
            AccountBalance destination = accountBalancePort.findByUserId(original.getDestinationUserId());
            if (destination.balance().compareTo(original.getAmount()) < 0) {
                return Optional.of("DESTINATION_INSUFFICIENT_BALANCE: O destino não tem saldo para devolver o valor");
            }
            return Optional.empty();
        } catch (AccountNotFoundException | AccountServiceUnavailableException e) {
            return Optional.of("BALANCE_SERVICE_UNAVAILABLE: Não foi possível confirmar o saldo do destino");
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

    private void updateBalances(Transaction tx, Transaction original) {
        if (tx.getStatus() != TransactionStatus.APPROVED) {
            return;
        }
        BigDecimal delta = tx.getType().isOutgoing() ? tx.getAmount().negate() : tx.getAmount();
        tryAdjustBalance(tx.getUserId(), delta, tx.getId());

        if (tx.getType() == TransactionType.TRANSFER) {
            tryAdjustBalance(tx.getDestinationUserId(), tx.getAmount(), tx.getId());
        }
        if (original != null && original.getType() == TransactionType.TRANSFER) {
            tryAdjustBalance(original.getDestinationUserId(), tx.getAmount().negate(), tx.getId());
        }
    }

    private void tryAdjustBalance(Long userId, BigDecimal delta, Long transactionId) {
        try {
            accountBalancePort.adjustBalance(userId, delta);
        } catch (AccountNotFoundException | AccountServiceUnavailableException e) {
            log.warn("Saldo não atualizado: transação {}, usuário {}: {}", transactionId, userId, e.getMessage());
        }
    }
}
