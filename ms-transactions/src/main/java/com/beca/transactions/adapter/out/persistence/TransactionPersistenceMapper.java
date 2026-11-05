package com.beca.transactions.adapter.out.persistence;

import com.beca.transactions.domain.model.ExchangeRate;
import com.beca.transactions.domain.model.ExchangeRates;
import com.beca.transactions.domain.model.Transaction;

final class TransactionPersistenceMapper {

    private TransactionPersistenceMapper() {
    }

    static TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getDestinationUserId(),
                transaction.getCorrectedTransactionId(),
                transaction.getStatus(),
                transaction.getRejectedReason(),
                transaction.getCreatedAt()
        );

        if (transaction.hasExchangeRates()) {
            ExchangeRates rates = transaction.getExchangeRates();
            entity.setUsdRate(rates.usd().rate());
            entity.setUsdRateDate(rates.usd().rateDate());
            entity.setEurRate(rates.eur().rate());
            entity.setEurRateDate(rates.eur().rateDate());
        }
        return entity;
    }

    static Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getDestinationUserId(),
                entity.getCorrectedTransactionId(),
                entity.getStatus(),
                entity.getRejectedReason(),
                exchangeRatesOf(entity),
                entity.getCreatedAt()
        );
    }

    // Transações sem taxa gravada (API fora do ar ou criadas antes do câmbio) voltam com null
    private static ExchangeRates exchangeRatesOf(TransactionEntity entity) {
        if (entity.getUsdRate() == null || entity.getEurRate() == null) {
            return null;
        }
        return new ExchangeRates(
                new ExchangeRate("USD", entity.getUsdRate(), entity.getUsdRateDate()),
                new ExchangeRate("EUR", entity.getEurRate(), entity.getEurRateDate())
        );
    }
}
