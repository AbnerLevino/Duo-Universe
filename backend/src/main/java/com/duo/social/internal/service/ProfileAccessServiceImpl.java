package com.duo.social.internal.service;

import com.duo.social.Access;
import com.duo.social.ProfileAccessService;
import com.duo.social.internal.model.FollowStatus;
import com.duo.social.internal.repository.BlockRepository;
import com.duo.social.internal.repository.FollowRepository;
import com.duo.users.ProfileVisibility;
import com.duo.users.UserService;
import com.duo.users.dto.UserProfile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A regra central de acesso (Épico 3). A ordem das perguntas importa: o dono sempre se vê; depois, tudo que
 * faz a pessoa "sumir" (oculta, bloqueio) vem antes de qualquer permissão; só então valem seguir e a
 * visibilidade do perfil.
 */
@Service
class ProfileAccessServiceImpl implements ProfileAccessService {

	private final UserService users;
	private final BlockRepository blocks;
	private final FollowRepository follows;

	ProfileAccessServiceImpl(UserService users, BlockRepository blocks, FollowRepository follows) {
		this.users = users;
		this.blocks = blocks;
		this.follows = follows;
	}

	@Override
	@Transactional(readOnly = true)
	public Access accessOf(Long viewerId, Long ownerId) {
		if (viewerId.equals(ownerId)) {
			return Access.OWNER;
		}
		return users.findProfileById(ownerId).map(owner -> accessTo(viewerId, owner)).orElse(Access.UNAVAILABLE);
	}

	@Override
	@Transactional(readOnly = true)
	public Access accessTo(Long viewerId, UserProfile owner) {
		if (viewerId.equals(owner.id())) {
			return Access.OWNER;
		}
		if (owner.hidden() || blocks.existsBetween(viewerId, owner.id())) {
			return Access.UNAVAILABLE;
		}
		boolean follower = follows.findByFollowerIdAndFolloweeId(viewerId, owner.id())
			.filter(follow -> follow.getStatus() == FollowStatus.ACCEPTED)
			.isPresent();
		if (follower) {
			return Access.FOLLOWER;
		}
		return owner.profileVisibility() == ProfileVisibility.PUBLIC ? Access.PUBLIC_VIEWER : Access.OUTSIDER;
	}
}
