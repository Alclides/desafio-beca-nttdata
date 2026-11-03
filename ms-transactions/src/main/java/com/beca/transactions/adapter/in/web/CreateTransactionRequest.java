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
        @Schema(example = "1", description = "Usuário dono da transação") @NotNull Long userId,
        @Schema(example = "PURCHASE") @NotNull TransactionType type,
        @Schema(example = "150.90", description = "Valor em reais, maior que zero, até 2 casas decimais")
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal amount,
        @Schema(example = "Mercado do mês") @Size(max = 255) String description,
        @Schema(example = "ALIMENTACAO", description = "Obrigatória em compra; nas demais, OUTROS quando não informada")
        Category category,
        @Schema(example = "2", description = "Obrigatório em transferência e proibido nos outros tipos")
        Long destinationUserId,
        @Schema(description = "Id da transação que esta corrige; a original continua gravada")
        Long correctedTransactionId
) {
    CreateTransactionCommand command() {
        return new CreateTransactionCommand(userId, type, amount, description, category, destinationUserId, correctedTransactionId);
    }
}
