package com.beca.transactions.application.port.out;

public interface UserDirectoryPort {
    boolean exists(Long userId);
}
