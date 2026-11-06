package com.duo.users.dto;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;

/** Tudo o que a pessoa edita no próprio perfil; a troca é completa (o app sempre manda todos os campos). */
public record ProfileUpdate(String displayName, String bio, String profession, MaritalStatus maritalStatus,
		ProfileVisibility profileVisibility) {
}
