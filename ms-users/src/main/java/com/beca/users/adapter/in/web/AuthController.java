package com.beca.users.adapter.in.web;

import com.beca.users.application.port.in.AuthenticateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Confere e-mail e senha")
@RestController
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @Operation(summary = "Confere e-mail e senha de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais corretas"),
            @ApiResponse(responseCode = "400", description = "E-mail ou senha ausentes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ErrorExamples.VALIDATION))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ErrorExamples.INVALID_CREDENTIALS)))
    })
    @PostMapping("/users/authenticate")
    public ResponseEntity<AuthenticatedUserResponse> authenticate(@Valid @RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(AuthenticatedUserResponse.from(
                authenticateUserUseCase.authenticate(request.email(), request.password())));
    }
}
