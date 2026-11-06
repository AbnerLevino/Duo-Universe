package com.duo.social.internal.service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import com.duo.shared.RateLimiter;
import com.duo.social.FollowService;
import com.duo.social.dto.ProfileCard;
import com.duo.social.dto.Relation;
import com.duo.social.exception.FollowLimitException;
import com.duo.social.exception.FollowRequestNotFoundException;
import com.duo.social.exception.InvalidSocialActionException;
import com.duo.social.internal.model.Follow;
import com.duo.social.internal.model.FollowStatus;
import com.duo.social.internal.repository.FollowRepository;
import com.duo.users.ProfileVisibility;
import com.duo.users.dto.UserProfile;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class FollowServiceImpl implements FollowService {

	private static final String RATE_SCOPE = "follow";
	private static final int MAX_ACTIONS_PER_HOUR = 60;
	private static final int MAX_PENDING_REQUESTS = 50;

	private final FollowRepository follows;
	private final ProfileLookup lookup;
	private final RateLimiter rateLimiter;
	private final Clock clock;

	FollowServiceImpl(FollowRepository follows, ProfileLookup lookup, RateLimiter rateLimiter, Clock clock) {
		this.follows = follows;
		this.lookup = lookup;
		this.rateLimiter = rateLimiter;
		this.clock = clock;
	}

	/**
	 * Sem transação própria de propósito (mesmo motivo do {@code findOrCreateByGoogle}): se dois cliques
	 * simultâneos tentam criar o mesmo Duollow, o índice único derruba um; dentro de uma transação externa
	 * isso a deixaria inutilizável e não daria para devolver o Duollow que o outro clique criou.
	 */
	@Override
	public Relation follow(Long me, String handle) {
		rateLimiter.checkAndRecord(RATE_SCOPE, me.toString(), MAX_ACTIONS_PER_HOUR, Duration.ofHours(1));
		UserProfile target = lookup.requireVisible(me, handle);
		if (target.id().equals(me)) {
			throw new InvalidSocialActionException("Você não pode seguir a si mesmo");
		}
		var existing = follows.findByFollowerIdAndFolloweeId(me, target.id());
		if (existing.isPresent()) {
			return SocialMapper.relationOf(existing.get());
		}
		boolean isPublic = target.profileVisibility() == ProfileVisibility.PUBLIC;
		if (!isPublic && follows.countByFollowerIdAndStatus(me, FollowStatus.PENDING) >= MAX_PENDING_REQUESTS) {
			throw new FollowLimitException();
		}
		Follow follow = new Follow(me, target.id(), isPublic ? FollowStatus.ACCEPTED : FollowStatus.PENDING);
		try {
			return SocialMapper.relationOf(follows.saveAndFlush(follow));
		}
		catch (DataIntegrityViolationException alreadyCreatedByAnotherClick) {
			return follows.findByFollowerIdAndFolloweeId(me, target.id())
				.map(SocialMapper::relationOf)
				.orElse(Relation.NONE);
		}
	}

	@Override
	@Transactional
	public void unfollow(Long me, String handle) {
		UserProfile target = lookup.requireExisting(handle);
		follows.deleteByPair(me, target.id());
	}

	@Override
	@Transactional
	public void acceptRequest(Long me, String followerHandle) {
		UserProfile follower = lookup.requireExisting(followerHandle);
		Follow request = follows.findByFollowerIdAndFolloweeId(follower.id(), me)
			.filter(follow -> follow.getStatus() == FollowStatus.PENDING)
			.orElseThrow(FollowRequestNotFoundException::new);
		request.accept(clock.instant());
		follows.save(request);
	}

	@Override
	@Transactional
	public void rejectRequest(Long me, String followerHandle) {
		UserProfile follower = lookup.requireExisting(followerHandle);
		follows.findByFollowerIdAndFolloweeId(follower.id(), me)
			.filter(follow -> follow.getStatus() == FollowStatus.PENDING)
			.ifPresent(follows::delete);
	}

	@Override
	@Transactional
	public void removeFollower(Long me, String followerHandle) {
		UserProfile follower = lookup.requireExisting(followerHandle);
		follows.deleteByPair(follower.id(), me);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileCard> pendingRequests(Long me) {
		return lookup.cards(follows.findTop200ByFolloweeIdAndStatusOrderByCreatedAtDesc(me, FollowStatus.PENDING)
			.stream()
			.map(Follow::getFollowerId)
			.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileCard> followers(Long me) {
		return lookup.cards(follows.findTop200ByFolloweeIdAndStatusOrderByCreatedAtDesc(me, FollowStatus.ACCEPTED)
			.stream()
			.map(Follow::getFollowerId)
			.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileCard> following(Long me) {
		return lookup.cards(follows.findTop200ByFollowerIdAndStatusOrderByCreatedAtDesc(me, FollowStatus.ACCEPTED)
			.stream()
			.map(Follow::getFolloweeId)
			.toList());
	}
}
