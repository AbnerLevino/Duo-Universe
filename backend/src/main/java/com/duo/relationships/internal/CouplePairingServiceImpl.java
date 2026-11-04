package com.duo.relationships.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import com.duo.relationships.Channel;
import com.duo.relationships.CouplePairingService;
import com.duo.relationships.InviteCodeInvalidException;
import com.duo.relationships.IssuedPairingInvite;
import com.duo.relationships.PairingNotFoundException;
import com.duo.relationships.PairingSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CouplePairingServiceImpl implements CouplePairingService {

	/** RN-29: pareamento sem decisão em 7 dias é tratado como desistência (limpeza de rascunhos: Etapas 6/7/12). */
	static final Duration TTL = Duration.ofDays(7);

	private static final int CODE_BYTES = 32;

	private final SecureRandom random = new SecureRandom();
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

	@Override
	@Transactional
	public IssuedPairingInvite inviteByLink(Long initiatorUserId) {
		byte[] bytes = new byte[CODE_BYTES];
		random.nextBytes(bytes);
		String rawCode = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		Instant expiresAt = clock.instant().plus(TTL);
		CouplePairing saved = pairings
			.save(new CouplePairing(initiatorUserId, null, Channel.LINK, expiresAt, hash(rawCode)));
		return new IssuedPairingInvite(saved.getId(), rawCode, expiresAt);
	}

	@Override
	@Transactional
	public PairingSummary redeemByLink(String code, Long inviteeUserId) {
		CouplePairing pairing = pairings.findByInviteCodeHash(hash(code)).orElseThrow(InviteCodeInvalidException::new);
		Instant now = clock.instant();
		if (pairing.isExpired(now)) {
			pairing.cancel(now);
		}
		pairing.redeemLink(inviteeUserId, now);
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

	static String hash(String raw) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 não está disponível", e);
		}
	}
}
