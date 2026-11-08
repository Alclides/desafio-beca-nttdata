package com.beca.users.config;

import com.beca.users.adapter.in.security.UserAuthenticationProvider;
import com.beca.users.adapter.in.web.SecurityErrorResponder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserAuthenticationProvider authenticationProvider,
                                                   SecurityErrorResponder errorResponder) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // Cadastro, importação e conferência de senha ficam abertos (pedido do PO)
                        .requestMatchers(HttpMethod.POST, "/users", "/users/upload", "/users/authenticate").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(errorResponder))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(errorResponder))
                .build();
    }
}
