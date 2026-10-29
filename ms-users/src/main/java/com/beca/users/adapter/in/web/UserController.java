package com.beca.users.adapter.in.web;

import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.CreateUserUseCase.CreateUserCommand;
import com.beca.users.application.port.in.DeleteUserUseCase;
import com.beca.users.application.port.in.FindUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.beca.users.domain.model.User;
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

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request,
                                               UriComponentsBuilder uriBuilder) {
        User user = createUserUseCase.create(
                new CreateUserCommand(request.name(), request.email(), request.password()));

        URI location = uriBuilder.path("/users/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserImportResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidCsvFileException("Arquivo CSV vazio");
        }
        try (InputStream input = file.getInputStream()) {
            return ResponseEntity.ok(csvUserImporter.importUsers(input));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(findUserUseCase.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = findUserUseCase.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUserUseCase.update(id, new UpdateUserCommand(request.name(), request.email()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
