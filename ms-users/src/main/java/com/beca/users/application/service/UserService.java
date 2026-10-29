package com.beca.users.application.service;

import com.beca.users.application.port.in.CreateUserUseCase;
import com.beca.users.application.port.in.DeleteUserUseCase;
import com.beca.users.application.port.in.FindUserUseCase;
import com.beca.users.application.port.in.UpdateUserUseCase;
import com.beca.users.application.port.out.PasswordEncoderPort;
import com.beca.users.application.port.out.UserRepositoryPort;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import com.beca.users.domain.exception.UserNotFoundException;
import com.beca.users.domain.model.User;

import java.time.Instant;
import java.util.List;

public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase {

    private final UserRepositoryPort repository;
    private final PasswordEncoderPort passwordEncoder;

    public UserService(UserRepositoryPort repository, PasswordEncoderPort passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(CreateUserCommand command) {
        if (repository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + command.email());
        }

        Instant now = Instant.now();
        User user = new User(
                null,
                command.name(),
                command.email(),
                passwordEncoder.encode(command.password()),
                now,
                now
        );
        return repository.save(user);
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + id));
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User update(Long id, UpdateUserCommand command) {
        User user = findById(id);

        boolean emailChanged = !user.getEmail().equals(command.email());
        if (emailChanged && repository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + command.email());
        }

        user.setName(command.name());
        user.setEmail(command.email());
        user.setUpdatedAt(Instant.now());
        return repository.save(user);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("Usuário não encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
