package com.beca.users.application.port.out;

import com.beca.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    void deleteById(Long id);
}
