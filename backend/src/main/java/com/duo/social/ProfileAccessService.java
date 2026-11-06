package com.duo.social;

import com.duo.users.dto.UserProfile;

/** API pública: "o que {@code viewer} pode ver de {@code owner}?". Outros módulos (mídias) só falam por aqui. */
public interface ProfileAccessService {

	Access accessOf(Long viewerId, Long ownerId);

	/** Igual a {@link #accessOf}, para quem já tem o perfil carregado (evita uma consulta). */
	Access accessTo(Long viewerId, UserProfile owner);
}
