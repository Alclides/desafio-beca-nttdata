package com.beca.users.application.port.in;

import com.beca.users.domain.model.User;

import java.util.List;

public interface FindUserUseCase {

    User findById(Long id);

    List<User> findAll();
}
