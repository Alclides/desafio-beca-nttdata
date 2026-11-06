package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.CreateTransactionUseCase;
import com.beca.transactions.application.port.in.FindTransactionUseCase;
import com.beca.transactions.domain.model.AuthenticatedUser;
import com.beca.transactions.domain.model.Transaction;
import com.beca.transactions.domain.model.TransactionType;
import com.beca.transactions.domain.service.OwnershipPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Transações", description = "Registro e consulta de transações financeiras")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindTransactionUseCase findTransactionUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 FindTransactionUseCase findTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionUseCase = findTransactionUseCase;
    }

    @Operation(summary = "Registra uma transação",
            description = "Depósito é aprovado direto. Retirada, transferência e compra passam pelas regras de saldo, "
                    + "limite por transação e limite diário. Aprovada ou rejeitada, a transação é gravada e volta com 201. "
                    + "O usuário e o destino são confirmados no serviço de usuários: quem não existe recebe 404, e com o "
                    + "serviço fora do ar a transação é gravada como REJECTED. "
                    + "Estorno: informe correctedTransactionId, o mesmo valor da original e o tipo inverso (DEPOSIT para "
                    + "saídas, WITHDRAWAL para depósitos); cada transação aprovada aceita um único estorno aprovado. "
                    + "O estorno de transferência debita o destino e é rejeitado se ele não tiver saldo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = CreateTransactionRequest.class),
            examples = {
                    @ExampleObject(name = "Compra", value = ApiExamples.PURCHASE),
                    @ExampleObject(name = "Depósito", value = ApiExamples.DEPOSIT),
                    @ExampleObject(name = "Retirada", value = ApiExamples.WITHDRAWAL),
                    @ExampleObject(name = "Transferência", value = ApiExamples.TRANSFER),
                    @ExampleObject(name = "Correção de uma transação", value = ApiExamples.CORRECTION)
            }))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transação gravada; veja o status e o motivo no corpo",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class),
                            examples = {
                                    @ExampleObject(name = "Aprovada", value = ApiExamples.APPROVED),
                                    @ExampleObject(name = "Rejeitada", value = ApiExamples.REJECTED)
                            })),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validação de campos", value = ApiExamples.VALIDATION),
                                    @ExampleObject(name = "Regra de negócio", value = ApiExamples.BUSINESS_RULE)
                            })),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Usuário, destino da transferência ou transação a corrigir não encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Usuário", value = ApiExamples.USER_NOT_FOUND),
                                    @ExampleObject(name = "Transação a corrigir", value = ApiExamples.NOT_FOUND)
                            }))
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateTransactionRequest request, UriComponentsBuilder uriBuilder) {
        OwnershipPolicy.ensureOwner(user.id(), request.userId());
        Transaction transaction = createTransactionUseCase.create(request.command());

        URI location = uriBuilder.path("/transactions/{id}").buildAndExpand(transaction.getId()).toUri();
        return ResponseEntity.created(location).body(TransactionResponse.from(transaction));
    }

    @Operation(summary = "Busca uma transação pelo ID",
            description = "Mostra o valor em real, dólar e euro, com a taxa e a data. Se a transação estiver sem taxa, "
                    + "tenta buscar de novo e grava; se a BrasilAPI falhar, responde com conversion.available = false.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transação encontrada"),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOT_FOUND)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        Transaction transaction = findTransactionUseCase.findById(id);
        OwnershipPolicy.ensureOwner(user.id(), transaction.getUserId());
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @Operation(summary = "Lista as transações de um usuário",
            description = "Ordenadas da mais recente para a mais antiga. Sem período informado, usa o mês atual. "
                    + "Não chama a BrasilAPI: transações sem taxa vêm com conversion.available = false.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de transações; vazia quando não há nenhuma no período"),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "400", description = "Parâmetro ausente, data em formato inválido ou período invertido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.INVALID_PERIOD)))
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findByUser(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Usuário dono das transações", example = "1")
            @RequestParam Long userId,
            @Parameter(description = "Data inicial (inclusiva), formato AAAA-MM-DD", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (inclusiva), formato AAAA-MM-DD", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filtra por tipo")
            @RequestParam(required = false) TransactionType type) {
        OwnershipPolicy.ensureOwner(user.id(), userId);
        List<TransactionResponse> transactions = findTransactionUseCase.findByUser(userId, from, to, type).stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(transactions);
    }
}
