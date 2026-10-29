package com.beca.users.config;

import com.beca.users.application.port.out.PasswordEncoderPort;
import com.beca.users.application.port.out.UserRepositoryPort;
import com.beca.users.application.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UserService userService(UserRepositoryPort repository, PasswordEncoderPort passwordEncoder) {
        return new UserService(repository, passwordEncoder);
    }
}
