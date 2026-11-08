package com.beca.users.adapter.in.security;

// Quem está logado. Vai para o contexto de segurança no lugar do User, para o hash da senha não circular
public record AuthenticatedUser(Long id, String name, String email) {
}
