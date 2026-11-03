package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.CreateTransactionUseCase.CreateTransactionCommand;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "PURCHASE") @NotNull TransactionType type,
        @Schema(example = "150.90") @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal amount,
        @Schema(example = "Mercado do Mês") @Size(max = 255) String description,
        @Schema(example = "ALIMENTACAO", description = "Obrigatorio caso seja uma compra") Category category,
        @Schema(description = "Obrigatorio em caso de transferencia") Long destinationUserId,
        @Schema(description = "Preencha para corrigir uma transação anterior") Long correctedTransactionId
) {
    CreateTransactionCommand command() {
        return new CreateTransactionCommand(userId, type, amount, description, category, destinationUserId, correctedTransactionId);
    }
}
