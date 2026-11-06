package com.duo.users.dto;

/** Visão pública de um usuário para os outros módulos (nunca expõe a entidade). {@code handle} é {@code null}
 * para quem ainda não escolheu o seu (RN-30). */
public record UserSummary(Long id, String email, String displayName, String handle) {
}
