package com.duo.social;

import com.duo.social.dto.PublicProfile;
import com.duo.social.dto.SearchPage;
import com.duo.social.exception.ProfileNotFoundException;

/** Ver o perfil de alguém e buscar pessoas por nome ou DuoName (decisão do fundador, B3/B4). */
public interface ProfileViewService {

	/** @throws ProfileNotFoundException se não existe, está oculto ou há bloqueio — a mesma resposta nos três casos */
	PublicProfile view(Long viewerId, String handle);

	SearchPage search(Long viewerId, String query, int page);
}
