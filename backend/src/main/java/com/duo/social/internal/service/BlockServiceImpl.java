package com.duo.social.internal.service;

import java.util.List;

import com.duo.social.BlockService;
import com.duo.social.dto.ProfileCard;
import com.duo.social.exception.InvalidSocialActionException;
import com.duo.social.internal.model.Block;
import com.duo.social.internal.repository.BlockRepository;
import com.duo.social.internal.repository.FollowRepository;
import com.duo.users.dto.UserProfile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BlockServiceImpl implements BlockService {

	private final BlockRepository blocks;
	private final FollowRepository follows;
	private final ProfileLookup lookup;

	BlockServiceImpl(BlockRepository blocks, FollowRepository follows, ProfileLookup lookup) {
		this.blocks = blocks;
		this.follows = follows;
		this.lookup = lookup;
	}

	/** Bloquear e desfazer os Duollows entre os dois acontecem juntos, numa transação só. */
	@Override
	@Transactional
	public void block(Long me, String handle) {
		UserProfile target = lookup.requireExisting(handle);
		if (target.id().equals(me)) {
			throw new InvalidSocialActionException("Você não pode bloquear a si mesmo");
		}
		if (!blocks.existsByBlockerIdAndBlockedId(me, target.id())) {
			blocks.save(new Block(me, target.id()));
		}
		follows.deleteBetween(me, target.id());
	}

	@Override
	@Transactional
	public void unblock(Long me, String handle) {
		UserProfile target = lookup.requireExisting(handle);
		blocks.deleteByPair(me, target.id());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileCard> blocked(Long me) {
		return lookup.cards(blocks.findTop200ByBlockerIdOrderByIdDesc(me).stream().map(Block::getBlockedId).toList());
	}
}
