package com.duo.social.internal.service;

import com.duo.social.dto.ProfileCard;
import com.duo.social.dto.Relation;
import com.duo.social.internal.model.Follow;
import com.duo.social.internal.model.FollowStatus;
import com.duo.users.dto.UserProfile;

/** Conversões pequenas e sem estado, compartilhadas pelos serviços do módulo. */
final class SocialMapper {

	private SocialMapper() {
	}

	static ProfileCard card(UserProfile profile) {
		return new ProfileCard(profile.id(), profile.displayName(), profile.handle(), profile.bio(),
				profile.profession(), profile.avatarMediaId());
	}

	static Relation relationOf(Follow follow) {
		return follow.getStatus() == FollowStatus.ACCEPTED ? Relation.FOLLOWING : Relation.REQUESTED;
	}
}
