package com.beca.users.adapter.in.web;

import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.application.port.in.DeleteUserUseCase;
import com.beca.users.application.port.in.FindUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.beca.users.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Tag(name = "Usuários", description = "Crud e importação de usuários")
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final FindUserUseCase findUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final CsvUserImporter csvUserImporter;

    public UserController(CreateUserUseCase createUserUseCase,
                          FindUserUseCase findUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          CsvUserImporter csvUserImporter) {
        this.createUserUseCase = createUserUseCase;
        this.findUserUseCase = findUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.csvUserImporter = csvUserImporter;
    }

    @Operation(summary = "Cria um novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
            description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400",
            description = "Falha na operação")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request,
                                               UriComponentsBuilder uriBuilder) {
        User user = createUserUseCase.create(
                new CreateUserCommand(request.name(), request.email(), request.password()));

        URI location = uriBuilder.path("/users/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }


    @Operation(summary = "Cria usuários a partir de um arquivo CSV")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuários criados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Falha na operação")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserImportResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidCsvFileException("Arquivo CSV vazio");
        }
        try (InputStream input = file.getInputStream()) {
            return ResponseEntity.ok(csvUserImporter.importUsers(input));
        }
    }


    @Operation(summary = "Busca um usuário pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(findUserUseCase.findById(id)));
    }


    @Operation(summary = "Busca todos os usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários encontrados"),
            @ApiResponse(responseCode = "404", description = "Erro ao procurar usuário")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = findUserUseCase.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Atualiza um usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Usuario atualizado com sucesso"),
            @ApiResponse(responseCode = "400",
                    description = "Falha na operação, dados inválidos"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUserUseCase.update(id, new UpdateUserCommand(request.name(), request.email()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(summary = "Deleta um usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
