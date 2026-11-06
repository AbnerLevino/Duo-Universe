package com.duo.users.dto;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;

/**
 * O perfil de uma pessoa, sem dado privado (nunca o e-mail). {@code hidden} é a moderação manual: quem
 * consome decide o que fazer (o módulo {@code social} trata um perfil oculto como inexistente para os outros).
 */
public record UserProfile(Long id, String displayName, String handle, String bio, String profession,
		MaritalStatus maritalStatus, ProfileVisibility profileVisibility, Long avatarMediaId, boolean hidden) {
}
