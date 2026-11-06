package com.duo.social.internal.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.duo.shared.RateLimiter;
import com.duo.social.Access;
import com.duo.social.ProfileAccessService;
import com.duo.social.ProfileViewService;
import com.duo.social.dto.PublicProfile;
import com.duo.social.dto.Relation;
import com.duo.social.dto.SearchItem;
import com.duo.social.dto.SearchPage;
import com.duo.social.exception.ProfileNotFoundException;
import com.duo.social.internal.model.Follow;
import com.duo.social.internal.model.FollowStatus;
import com.duo.social.internal.repository.BlockRepository;
import com.duo.social.internal.repository.FollowRepository;
import com.duo.users.UserService;
import com.duo.users.dto.ProfilePage;
import com.duo.users.dto.UserProfile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProfileViewServiceImpl implements ProfileViewService {

	private static final String SEARCH_SCOPE = "profile_search";
	private static final int MAX_SEARCHES_PER_HOUR = 120;
	private static final int PAGE_SIZE = 20;
	private static final int MAX_PAGE = 50;

	private final UserService users;
	private final ProfileAccessService access;
	private final FollowRepository follows;
	private final BlockRepository blocks;
	private final RateLimiter rateLimiter;

	ProfileViewServiceImpl(UserService users, ProfileAccessService access, FollowRepository follows,
			BlockRepository blocks, RateLimiter rateLimiter) {
		this.users = users;
		this.access = access;
		this.follows = follows;
		this.blocks = blocks;
		this.rateLimiter = rateLimiter;
	}

	@Override
	@Transactional(readOnly = true)
	public PublicProfile view(Long viewerId, String handle) {
		UserProfile profile = users.findProfileByHandle(handle).orElseThrow(ProfileNotFoundException::new);
		Access viewerAccess = access.accessTo(viewerId, profile);
		if (!viewerAccess.canSeeCard()) {
			throw new ProfileNotFoundException();
		}
		boolean self = viewerAccess == Access.OWNER;
		Relation relation = self ? Relation.NONE
				: follows.findByFollowerIdAndFolloweeId(viewerId, profile.id())
					.map(SocialMapper::relationOf)
					.orElse(Relation.NONE);
		return new PublicProfile(profile.id(), profile.displayName(), profile.handle(), profile.bio(),
				profile.profession(), profile.maritalStatus(), profile.profileVisibility(), profile.avatarMediaId(),
				follows.countByFolloweeIdAndStatus(profile.id(), FollowStatus.ACCEPTED),
				follows.countByFollowerIdAndStatus(profile.id(), FollowStatus.ACCEPTED), relation, self,
				viewerAccess.canSeeContent());
	}

	/**
	 * Sem {@code readOnly}: o limite de tentativas grava uma linha (RA-08). Quem bloqueou ou foi bloqueado,
	 * e a própria pessoa, não aparecem nos resultados.
	 */
	@Override
	public SearchPage search(Long viewerId, String query, int page) {
		rateLimiter.checkAndRecord(SEARCH_SCOPE, viewerId.toString(), MAX_SEARCHES_PER_HOUR, Duration.ofHours(1));
		Set<Long> excluded = new HashSet<>(blocks.relatedIds(viewerId));
		excluded.add(viewerId);
		ProfilePage found = users.searchProfiles(query, excluded, Math.min(Math.max(page, 0), MAX_PAGE), PAGE_SIZE);
		List<Long> ids = found.items().stream().map(UserProfile::id).toList();
		Map<Long, Relation> relations = ids.isEmpty() ? Map.of()
				: follows.findByFollowerIdAndFolloweeIdIn(viewerId, ids)
					.stream()
					.collect(Collectors.toMap(Follow::getFolloweeId, SocialMapper::relationOf, (a, b) -> a));
		List<SearchItem> items = found.items()
			.stream()
			.map(profile -> new SearchItem(SocialMapper.card(profile),
					relations.getOrDefault(profile.id(), Relation.NONE)))
			.toList();
		return new SearchPage(items, found.hasMore());
	}
}
