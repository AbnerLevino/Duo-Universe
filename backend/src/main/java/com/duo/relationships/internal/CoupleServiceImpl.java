package com.duo.relationships.internal;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.duo.relationships.CoupleNotFoundException;
import com.duo.relationships.CoupleService;
import com.duo.relationships.CoupleSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CoupleServiceImpl implements CoupleService {

	private final CoupleRepository couples;
	private final CoupleMemberRepository members;
	private final Clock clock;

	CoupleServiceImpl(CoupleRepository couples, CoupleMemberRepository members, Clock clock) {
		this.couples = couples;
		this.members = members;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public CoupleSummary find(Long coupleId, Long userId) {
		return toSummary(findVisibleTo(coupleId, userId));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CoupleSummary> findActiveForUser(Long userId) {
		return members.findByUserIdAndLeftAtIsNull(userId)
			.flatMap(member -> couples.findById(member.getCoupleId()))
			.map(CoupleServiceImpl::toSummary);
	}

	@Override
	@Transactional
	public CoupleSummary pause(Long coupleId, Long userId) {
		Couple couple = findVisibleTo(coupleId, userId);
		couple.pause(clock.instant());
		return toSummary(couple);
	}

	@Override
	@Transactional
	public CoupleSummary resume(Long coupleId, Long userId) {
		Couple couple = findVisibleTo(coupleId, userId);
		couple.resume(clock.instant());
		return toSummary(couple);
	}

	@Override
	@Transactional
	public void leave(Long coupleId, Long userId) {
		Couple couple = findVisibleTo(coupleId, userId);
		Instant now = clock.instant();
		// RN-07 (MVP com 2 membros): sair de um termina o vínculo inteiro — a saída individual (RN-14) de
		// cada um fica registrada pelo mesmo "left_at", mas os dois perdem o acesso ao Couple Universe.
		List<CoupleMember> stillIn = members.findByCoupleIdAndLeftAtIsNull(coupleId);
		stillIn.forEach(member -> member.leave(now));
		couple.end(now);
	}

	private Couple findVisibleTo(Long coupleId, Long userId) {
		members.findByCoupleIdAndUserIdAndLeftAtIsNull(coupleId, userId).orElseThrow(CoupleNotFoundException::new);
		return couples.findById(coupleId).orElseThrow(CoupleNotFoundException::new);
	}

	private static CoupleSummary toSummary(Couple couple) {
		return new CoupleSummary(couple.getId(), couple.getState(), couple.getKnownSinceDate(),
				couple.getDatingSinceDate(), couple.getPausedAt(), couple.getEndedAt());
	}
}
