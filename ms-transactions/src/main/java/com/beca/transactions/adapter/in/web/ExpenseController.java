package com.beca.transactions.adapter.in.web;

import com.beca.transactions.application.port.in.AnalyzeExpensesUseCase;
import com.beca.transactions.domain.model.AuthenticatedUser;
import com.beca.transactions.domain.model.Category;
import com.beca.transactions.domain.service.OwnershipPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@Tag(name = "Despesas", description = "Análise de despesas por categoria, dia e mês")
@RestController
@RequestMapping("/users/{userId}/expenses")
public class ExpenseController {

    private static final String RULES = "Contam só retiradas, transferências enviadas e compras aprovadas, sem estornos. "
            + "Sem período informado, usa o mês atual.";

    private final AnalyzeExpensesUseCase analyzeExpensesUseCase;

    public ExpenseController(AnalyzeExpensesUseCase analyzeExpensesUseCase) {
        this.analyzeExpensesUseCase = analyzeExpensesUseCase;
    }

    @Operation(summary = "Total de despesas por categoria", description = RULES)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total de cada categoria e total geral",
                    content = @Content(examples = @ExampleObject(value = ApiExamples.EXPENSES_BY_CATEGORY))),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "400", description = "Data em formato inválido ou período invertido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.INVALID_PERIOD)))
    })
    @GetMapping("/summary")
    public ResponseEntity<ExpenseReportResponse<Category>> summary(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Usuário dono das despesas", example = "1") @PathVariable Long userId,
            @Parameter(description = "Data inicial (inclusiva), formato AAAA-MM-DD", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (inclusiva), formato AAAA-MM-DD", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        OwnershipPolicy.ensureOwner(user.id(), userId);
        return ResponseEntity.ok(ExpenseReportResponse.from(analyzeExpensesUseCase.byCategory(userId, from, to)));
    }

    @Operation(summary = "Total de despesas por dia", description = RULES + " Dias sem despesa não aparecem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total de cada dia e total geral",
                    content = @Content(examples = @ExampleObject(value = ApiExamples.EXPENSES_BY_DAY))),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "400", description = "Data em formato inválido ou período invertido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.INVALID_PERIOD)))
    })
    @GetMapping("/daily")
    public ResponseEntity<ExpenseReportResponse<LocalDate>> daily(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Usuário dono das despesas", example = "1") @PathVariable Long userId,
            @Parameter(description = "Data inicial (inclusiva), formato AAAA-MM-DD", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (inclusiva), formato AAAA-MM-DD", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        OwnershipPolicy.ensureOwner(user.id(), userId);
        return ResponseEntity.ok(ExpenseReportResponse.from(analyzeExpensesUseCase.byDay(userId, from, to)));
    }

    @Operation(summary = "Total de despesas por mês", description = RULES)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total de cada mês e total geral",
                    content = @Content(examples = @ExampleObject(value = ApiExamples.EXPENSES_BY_MONTH))),
            @ApiResponse(responseCode = "403", description = "Os dados pertencem a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "400", description = "Data em formato inválido ou período invertido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiExamples.INVALID_PERIOD)))
    })
    @GetMapping("/monthly")
    public ResponseEntity<ExpenseReportResponse<YearMonth>> monthly(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Usuário dono das despesas", example = "1") @PathVariable Long userId,
            @Parameter(description = "Data inicial (inclusiva), formato AAAA-MM-DD", example = "2026-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (inclusiva), formato AAAA-MM-DD", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        OwnershipPolicy.ensureOwner(user.id(), userId);
        return ResponseEntity.ok(ExpenseReportResponse.from(analyzeExpensesUseCase.byMonth(userId, from, to)));
    }
}
