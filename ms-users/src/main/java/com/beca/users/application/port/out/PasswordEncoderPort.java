package com.beca.users.application.port.out;

public interface PasswordEncoderPort {

    String encode(String rawPassword);
}
