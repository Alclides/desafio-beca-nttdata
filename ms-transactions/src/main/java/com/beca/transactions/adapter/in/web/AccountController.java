package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.FindAccountBalanceUseCase;
import com.beca.transactions.domain.model.AuthenticatedUser;
import com.beca.transactions.domain.service.OwnershipPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contas", description = "Consulta de saldo e limites da conta")
@RestController
public class AccountController {

    private final FindAccountBalanceUseCase findAccountBalanceUseCase;

    public AccountController(FindAccountBalanceUseCase findAccountBalanceUseCase) {
        this.findAccountBalanceUseCase = findAccountBalanceUseCase;
    }

    @Operation(summary = "Consulta o saldo e os limites de um usuário",
            description = "Devolve os valores que estão na API Mock no momento da consulta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo e limites da conta",
                    content = @Content(schema = @Schema(implementation = AccountBalanceResponse.class),
                            examples = @ExampleObject(value = ApiExamples.BALANCE))),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Usuário sem conta na API Mock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.ACCOUNT_NOT_FOUND))),
            @ApiResponse(responseCode = "503", description = "API Mock fora do ar ou sem responder a tempo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.BALANCE_UNAVAILABLE)))
    })
    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<AccountBalanceResponse> findBalance(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Usuário dono da conta", example = "1") @PathVariable Long userId) {
        OwnershipPolicy.ensureOwner(user.id(), userId);
        return ResponseEntity.ok(AccountBalanceResponse.from(findAccountBalanceUseCase.findByUserId(userId)));
    }
}
