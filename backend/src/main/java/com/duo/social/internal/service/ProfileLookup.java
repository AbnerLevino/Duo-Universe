package com.duo.social.internal.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.duo.social.ProfileAccessService;
import com.duo.social.dto.ProfileCard;
import com.duo.social.exception.ProfileNotFoundException;
import com.duo.users.UserService;
import com.duo.users.dto.UserProfile;

import org.springframework.stereotype.Component;

/**
 * Achar uma pessoa pelo DuoName como o visitante a enxerga: se ela não existe, está oculta ou há bloqueio, o
 * resultado é sempre o mesmo erro. Todo serviço do módulo que recebe um DuoName passa por aqui.
 */
@Component
class ProfileLookup {

	private final UserService users;
	private final ProfileAccessService access;

	ProfileLookup(UserService users, ProfileAccessService access) {
		this.users = users;
		this.access = access;
	}

	UserProfile requireVisible(Long viewerId, String handle) {
		return users.findProfileByHandle(handle)
			.filter(profile -> access.accessTo(viewerId, profile).canSeeCard())
			.orElseThrow(ProfileNotFoundException::new);
	}

	/** Existe e não foi excluída; serve para ações que não devem ser barradas por bloqueio (ex.: desbloquear). */
	UserProfile requireExisting(String handle) {
		return users.findProfileByHandle(handle).orElseThrow(ProfileNotFoundException::new);
	}

	/** Os cartões na mesma ordem dos ids dados, sem as contas ocultas pela moderação. */
	List<ProfileCard> cards(Collection<Long> orderedIds) {
		Map<Long, UserProfile> byId = users.findProfilesByIds(orderedIds)
			.stream()
			.filter(profile -> !profile.hidden())
			.collect(Collectors.toMap(UserProfile::id, Function.identity()));
		return orderedIds.stream().map(byId::get).filter(java.util.Objects::nonNull).map(SocialMapper::card).toList();
	}
}
