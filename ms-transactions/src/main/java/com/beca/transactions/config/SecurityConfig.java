package com.beca.transactions.config;

import com.beca.transactions.adapter.in.security.UserServiceAuthenticationProvider;
import com.beca.transactions.adapter.in.web.SecurityErrorResponder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserServiceAuthenticationProvider authenticationProvider,
                                                   SecurityErrorResponder errorResponder) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(errorResponder))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(errorResponder))
                .build();
    }
}
