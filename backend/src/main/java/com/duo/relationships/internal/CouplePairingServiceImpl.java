package com.duo.relationships.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import com.duo.relationships.AlreadyInRelationshipException;
import com.duo.relationships.Channel;
import com.duo.relationships.CouplePairingService;
import com.duo.relationships.DatesNotAcceptedException;
import com.duo.relationships.HandleNotInvitableException;
import com.duo.relationships.InviteCodeInvalidException;
import com.duo.relationships.IssuedPairingInvite;
import com.duo.relationships.PairingNotFoundException;
import com.duo.relationships.PairingState;
import com.duo.relationships.PairingSummary;
import com.duo.relationships.StoryIncompleteException;
import com.duo.relationships.StoryStep;
import com.duo.relationships.StorySummary;
import com.duo.shared.RateLimiter;
import com.duo.users.UserService;
import com.duo.users.UserSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CouplePairingServiceImpl implements CouplePairingService {

	/** RN-29: pareamento sem decisão em 7 dias é tratado como desistência (limpeza de rascunhos: Etapas 6/7/12). */
	static final Duration TTL = Duration.ofDays(7);

	/** RA-08: ajustável — não é uma regra de negócio do vínculo, é só o tamanho do freio contra força bruta. */
	static final String INVITE_BY_HANDLE_SCOPE = "pairing_invite_by_handle";

	static final int INVITE_BY_HANDLE_MAX_ATTEMPTS = 20;

	static final Duration INVITE_BY_HANDLE_WINDOW = Duration.ofHours(1);

	private static final int CODE_BYTES = 32;

	private final SecureRandom random = new SecureRandom();
	private final CouplePairingRepository pairings;
	private final CoupleRepository couples;
	private final CoupleMemberRepository coupleMembers;
	private final CouplePairingStoryRepository stories;
	private final UserService users;
	private final RateLimiter rateLimiter;
	private final CoupleFactory coupleFactory;
	private final Clock clock;

	CouplePairingServiceImpl(CouplePairingRepository pairings, CoupleRepository couples,
			CoupleMemberRepository coupleMembers, CouplePairingStoryRepository stories, UserService users,
			RateLimiter rateLimiter, CoupleFactory coupleFactory, Clock clock) {
		this.pairings = pairings;
		this.couples = couples;
		this.coupleMembers = coupleMembers;
		this.stories = stories;
		this.users = users;
		this.rateLimiter = rateLimiter;
		this.coupleFactory = coupleFactory;
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
	public PairingSummary inviteByHandle(Long initiatorUserId, String handle) {
		rateLimiter.checkAndRecord(
				INVITE_BY_HANDLE_SCOPE, initiatorUserId.toString(), INVITE_BY_HANDLE_MAX_ATTEMPTS,
				INVITE_BY_HANDLE_WINDOW);
		Long inviteeUserId = users.findByHandle(handle)
			.map(UserSummary::id)
			.filter(id -> !id.equals(initiatorUserId))
			.filter(id -> coupleMembers.findByUserIdAndLeftAtIsNull(id).isEmpty())
			.orElseThrow(HandleNotInvitableException::new);
		return initiate(initiatorUserId, inviteeUserId, Channel.HANDLE);
	}

	@Override
	@Transactional
	public void cancel(Long pairingId, Long userId) {
		cancelAndEraseDrafts(findVisibleTo(pairingId, userId), clock.instant());
	}

	@Override
	@Transactional
	public PairingSummary find(Long pairingId, Long userId) {
		CouplePairing pairing = findVisibleTo(pairingId, userId);
		Instant now = clock.instant();
		if (pairing.isExpired(now)) {
			cancelAndEraseDrafts(pairing, now);
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
			cancelAndEraseDrafts(pairing, now);
		}
		pairing.redeemLink(inviteeUserId, now);
		return toSummary(pairing);
	}

	/**
	 * RN-08: trava a linha do pareamento ({@code findByIdForUpdate}) para que as duas confirmações do mesmo
	 * pareamento nunca sejam processadas "ao mesmo tempo" — sem isso, as duas poderiam ler o dado antes da
	 * outra escrever, e uma confirmação legítima se perderia (um "lost update" clássico).
	 *
	 * <p>{@code noRollbackFor}: se {@link CoupleFactory#createFrom} falhar porque a pessoa já está em outro
	 * vínculo (RN-06), a exceção escapa até aqui — mas sem essa anotação, o Spring desfaria esta transação
	 * inteira, inclusive a confirmação (gravada por {@code recordConfirmation}, logo abaixo) que é legítima e
	 * deve continuar valendo.
	 */
	@Override
	@Transactional(noRollbackFor = AlreadyInRelationshipException.class)
	public PairingSummary confirm(Long pairingId, Long userId) {
		CouplePairing pairing = pairings.findByIdForUpdate(pairingId)
			.filter(p -> p.involves(userId))
			.orElseThrow(PairingNotFoundException::new);
		Instant now = clock.instant();
		if (pairing.isExpired(now)) {
			cancelAndEraseDrafts(pairing, now);
			return toSummary(pairing);
		}
		pairing.recordConfirmation(userId, now);
		if (pairing.isFullyConfirmed()) {
			Long coupleId = coupleFactory.createFrom(pairing.getInitiatorUserId(), pairing.getInviteeUserId());
			pairing.markConfirmed(now, coupleId);
		}
		return toSummary(pairing);
	}

	@Override
	@Transactional
	public void saveStoryDraft(Long pairingId, Long userId, StoryStep step, String text) {
		findVisibleTo(pairingId, userId).requireConfirmed();
		CouplePairingStory story = stories.findByPairingIdAndUserId(pairingId, userId)
			.orElseGet(() -> stories.save(new CouplePairingStory(pairingId, userId)));
		story.updateStep(step, text);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isStoryComplete(Long pairingId, Long userId) {
		findVisibleTo(pairingId, userId);
		return storyIsComplete(pairingId, userId);
	}

	@Override
	@Transactional(readOnly = true)
	public void requireStoryComplete(Long pairingId, Long userId) {
		if (!isStoryComplete(pairingId, userId)) {
			throw new StoryIncompleteException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StorySummary> readPartnerStory(Long pairingId, Long userId) {
		CouplePairing pairing = findVisibleTo(pairingId, userId);
		Long partnerId = pairing.partnerOf(userId);
		if (partnerId == null || !storyIsComplete(pairingId, userId) || !storyIsComplete(pairingId, partnerId)) {
			return Optional.empty();
		}
		return stories.findByPairingIdAndUserId(pairingId, partnerId).map(CouplePairingServiceImpl::toStorySummary);
	}

	private boolean storyIsComplete(Long pairingId, Long userId) {
		return stories.findByPairingIdAndUserId(pairingId, userId).map(CouplePairingStory::isComplete).orElse(false);
	}

	private static StorySummary toStorySummary(CouplePairingStory story) {
		return new StorySummary(story.getPairingId(), story.getUserId(), story.getHowWeMet(),
				story.getTimeBeforeDating(), story.getWhatIThoughtThen(), story.getWhatIThinkNow());
	}

	@Override
	@Transactional
	public void proposeDates(Long pairingId, Long userId, LocalDate knownSinceDate, LocalDate datingSinceDate) {
		findVisibleTo(pairingId, userId).proposeDates(userId, knownSinceDate, datingSinceDate);
	}

	@Override
	@Transactional
	public void acceptDates(Long pairingId, Long userId) {
		CouplePairing pairing = findVisibleTo(pairingId, userId);
		pairing.acceptDates(userId, clock.instant());
		// RN-28: o Couple nasceu vazio (Etapa 8); agora que as duas datas foram aceitas, copiamos para ele —
		// coupleId nunca é nulo aqui, porque acceptDates só funciona com o pareamento já CONFIRMED.
		Couple couple = couples.findById(pairing.getCoupleId()).orElseThrow();
		couple.recordDates(pairing.getProposedKnownSinceDate(), pairing.getProposedDatingSinceDate());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean areDatesAccepted(Long pairingId, Long userId) {
		return findVisibleTo(pairingId, userId).areDatesAccepted();
	}

	@Override
	@Transactional(readOnly = true)
	public void requireDatesAccepted(Long pairingId, Long userId) {
		if (!areDatesAccepted(pairingId, userId)) {
			throw new DatesNotAcceptedException();
		}
	}

	/** RN-29: cancelar, recusar ou vencer apaga os rascunhos — a proposta de datas vive na própria entidade
	 * ({@link CouplePairing#cancel}), mas a história mora numa tabela separada (Etapa 6), por isso precisa
	 * ser apagada por aqui também. */
	private void cancelAndEraseDrafts(CouplePairing pairing, Instant when) {
		pairing.cancel(when);
		stories.deleteAllByPairingId(pairing.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<PairingSummary> findPendingForUser(Long userId) {
		return pairings.findByUserIdAndState(userId, PairingState.PENDING).stream()
			.map(CouplePairingServiceImpl::toSummary)
			.toList();
	}

	private CouplePairing findVisibleTo(Long pairingId, Long userId) {
		return pairings.findById(pairingId)
			.filter(pairing -> pairing.involves(userId))
			.orElseThrow(PairingNotFoundException::new);
	}

	private static PairingSummary toSummary(CouplePairing pairing) {
		return new PairingSummary(pairing.getId(), pairing.getInitiatorUserId(), pairing.getInviteeUserId(),
				pairing.getChannel(), pairing.getState(), pairing.getExpiresAt(), pairing.getCreatedAt(),
				pairing.getCoupleId());
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
