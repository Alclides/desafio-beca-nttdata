package com.beca.users.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
