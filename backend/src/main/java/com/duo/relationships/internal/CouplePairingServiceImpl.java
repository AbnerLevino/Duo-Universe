package com.duo.relationships.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.duo.relationships.Channel;
import com.duo.relationships.CouplePairingService;
import com.duo.relationships.PairingNotFoundException;
import com.duo.relationships.PairingSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CouplePairingServiceImpl implements CouplePairingService {

	/** RN-29: pareamento sem decisão em 7 dias é tratado como desistência (limpeza de rascunhos: Etapas 6/7/12). */
	static final Duration TTL = Duration.ofDays(7);

	private final CouplePairingRepository pairings;
	private final Clock clock;

	CouplePairingServiceImpl(CouplePairingRepository pairings, Clock clock) {
		this.pairings = pairings;
		this.clock = clock;
	}

	@Override
	@Transactional
	public PairingSummary initiate(Long initiatorUserId, Long inviteeUserId, Channel channel) {
		Instant expiresAt = clock.instant().plus(TTL);
		CouplePairing saved = pairings.save(new CouplePairing(initiatorUserId, inviteeUserId, channel, expiresAt));
		return toSummary(saved);
	}

	@Override
	@Transactional
	public void cancel(Long pairingId, Long userId) {
		findVisibleTo(pairingId, userId).cancel(clock.instant());
	}

	@Override
	@Transactional
	public PairingSummary find(Long pairingId, Long userId) {
		CouplePairing pairing = findVisibleTo(pairingId, userId);
		Instant now = clock.instant();
		if (pairing.isExpired(now)) {
			pairing.cancel(now);
		}
		return toSummary(pairing);
	}

	private CouplePairing findVisibleTo(Long pairingId, Long userId) {
		return pairings.findById(pairingId)
			.filter(pairing -> pairing.involves(userId))
			.orElseThrow(PairingNotFoundException::new);
	}

	private static PairingSummary toSummary(CouplePairing pairing) {
		return new PairingSummary(pairing.getId(), pairing.getInitiatorUserId(), pairing.getInviteeUserId(),
				pairing.getChannel(), pairing.getState(), pairing.getExpiresAt(), pairing.getCreatedAt());
	}
}
