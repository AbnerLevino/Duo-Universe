package com.duo.users;

/** Visão pública de um usuário para os outros módulos (nunca expõe a entidade). */
public record UserSummary(Long id, String email, String displayName) {
}
