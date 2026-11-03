package com.beca.users.adapter.out.persistence;

import com.beca.users.application.port.out.UserRepositoryPort;
import com.beca.users.domain.exception.EmailAlreadyExistsException;
import com.beca.users.domain.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        try {
            UserEntity saved = repository.save(UserPersistenceMapper.toEntity(user));
            return UserPersistenceMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            // Duas requisições com o mesmo e-mail podem passar juntas pelo existsByEmail do service;
            // a constraint unique da coluna email barra a segunda, e aqui ela vira o erro de domínio (409)
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + user.getEmail());
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(UserPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
