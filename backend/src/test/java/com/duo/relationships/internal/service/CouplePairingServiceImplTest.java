package com.duo.relationships.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import com.duo.relationships.Channel;
import com.duo.relationships.CoupleState;
import com.duo.relationships.PairingState;
import com.duo.relationships.StoryStep;
import com.duo.relationships.dto.IssuedPairingInvite;
import com.duo.relationships.dto.PairingSummary;
import com.duo.relationships.dto.StorySummary;
import com.duo.relationships.exception.AlreadyInRelationshipException;
import com.duo.relationships.exception.DatesNotAcceptedException;
import com.duo.relationships.exception.HandleNotInvitableException;
import com.duo.relationships.exception.IllegalPairingOperationException;
import com.duo.relationships.exception.InviteCodeInvalidException;
import com.duo.relationships.exception.PairingNotFoundException;
import com.duo.relationships.exception.StoryIncompleteException;
import com.duo.relationships.internal.model.Couple;
import com.duo.relationships.internal.model.CoupleMember;
import com.duo.relationships.internal.model.CouplePairing;
import com.duo.relationships.internal.repository.CoupleMemberRepository;
import com.duo.relationships.internal.repository.CouplePairingRepository;
import com.duo.relationships.internal.repository.CouplePairingStoryRepository;
import com.duo.relationships.internal.repository.CoupleRepository;
import com.duo.shared.RateLimiter;
import com.duo.shared.exception.RateLimitExceededException;
import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/** RN-08/RN-29 contra o banco de verdade; o serviço é montado à mão para controlar o relógio (ver RefreshTokenServiceImplTest). */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouplePairingServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

	@Autowired
	CouplePairingRepository pairings;

	@Autowired
	CoupleRepository couples;

	@Autowired
	CoupleMemberRepository coupleMembers;

	@Autowired
	CouplePairingStoryRepository stories;

	@Autowired
	UserRepository users;

	Long initiatorId;
	Long inviteeId;

	@BeforeEach
	void createUsers() {
		initiatorId = users.save(new User("sub-cp-a", "cp-a@example.com", "A")).getId();
		inviteeId = users.save(new User("sub-cp-b", "cp-b@example.com", "B")).getId();
	}

	private CouplePairingServiceImpl serviceAt(Instant now) {
		return serviceAt(now, alwaysAllow());
	}

	private CouplePairingServiceImpl serviceAt(Instant now, RateLimiter rateLimiter) {
		return new CouplePairingServiceImpl(pairings, couples, coupleMembers, stories, fakeUserService(),
				rateLimiter, new CoupleFactory(couples, coupleMembers), Clock.fixed(now, ZoneOffset.UTC));
	}

	/** As duas pessoas já confirmaram (RN-08) — o Couple nasceu, vazio; história e datas já podem ser escritas. */
	private PairingSummary confirmedPairing(CouplePairingServiceImpl service) {
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		service.confirm(pairing.id(), initiatorId);
		return service.confirm(pairing.id(), inviteeId);
	}

	/**
	 * Um dublê simples: só {@code findByHandle} importa para este teste (o resto do módulo {@code users} já
	 * tem os próprios testes, Etapa 4). Devolve dados reais do {@code UserRepository} autowired acima, então
	 * continua testando contra o {@code duo_test} de verdade.
	 */
	private UserService fakeUserService() {
		return new UserService() {
			@Override
			public UserSummary findOrCreateByGoogle(String googleSub, String email, String displayName) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<UserSummary> findByGoogleSub(String googleSub) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<UserSummary> findById(Long id) {
				throw new UnsupportedOperationException();
			}

			@Override
			public UserSummary chooseHandle(Long userId, String handle) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<UserSummary> findByHandle(String handle) {
				return users.findByHandleIgnoreCase(handle)
					.map(u -> new UserSummary(u.getId(), u.getEmail(), u.getDisplayName(), u.getHandle()));
			}
		};
	}

	private static RateLimiter alwaysAllow() {
		return (scope, key, maxAttempts, window) -> {
		};
	}

	private static RateLimiter alwaysBlock() {
		return (scope, key, maxAttempts, window) -> {
			throw new RateLimitExceededException();
		};
	}

	private void giveHandle(Long userId, String handle) {
		User user = users.findById(userId).orElseThrow();
		user.setHandle(handle);
		users.saveAndFlush(user);
	}

	@Test
	void initiateCreatesAPendingPairingThatExpiresInSevenDays() {
		PairingSummary summary = serviceAt(NOW).initiate(initiatorId, inviteeId, Channel.HANDLE);

		assertThat(summary.state()).isEqualTo(PairingState.PENDING);
		assertThat(summary.expiresAt()).isEqualTo(NOW.plus(CouplePairingServiceImpl.TTL));
	}

	@Test
	void eitherPersonCanCancelAPendingPairing() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		service.cancel(pairing.id(), inviteeId);

		assertThat(service.find(pairing.id(), initiatorId).state()).isEqualTo(PairingState.CANCELLED);
	}

	@Test
	void someoneOutsideThePairingCannotSeeOrCancelIt() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Long outsider = users.save(new User("sub-cp-c", "cp-c@example.com", "C")).getId();

		assertThatThrownBy(() -> service.find(pairing.id(), outsider))
				.isInstanceOf(PairingNotFoundException.class);
		assertThatThrownBy(() -> service.cancel(pairing.id(), outsider))
				.isInstanceOf(PairingNotFoundException.class);
	}

	@Test
	void findLazilyCancelsAPendingPairingThatAlreadyExpired() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Instant afterExpiry = NOW.plus(CouplePairingServiceImpl.TTL).plusSeconds(1);

		PairingSummary found = serviceAt(afterExpiry).find(pairing.id(), initiatorId);

		assertThat(found.state()).isEqualTo(PairingState.CANCELLED);
	}

	@Test
	void findDoesNotTouchAPairingThatIsStillWithinTheDeadline() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		PairingSummary found = service.find(pairing.id(), initiatorId);

		assertThat(found.state()).isEqualTo(PairingState.PENDING);
	}

	@Test
	void inviteByLinkStoresOnlyTheHashNeverTheCode() {
		IssuedPairingInvite invite = serviceAt(NOW).inviteByLink(initiatorId);

		assertThat(pairings.findByInviteCodeHash(invite.code())).isEmpty();
		assertThat(pairings.findByInviteCodeHash(CouplePairingServiceImpl.hash(invite.code()))).isPresent();
		assertThat(invite.expiresAt()).isEqualTo(NOW.plus(CouplePairingServiceImpl.TTL));
	}

	@Test
	void redeemByLinkAttachesTheInviteeToThePairing() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		IssuedPairingInvite invite = service.inviteByLink(initiatorId);

		PairingSummary redeemed = service.redeemByLink(invite.code(), inviteeId);

		assertThat(redeemed.inviteeUserId()).isEqualTo(inviteeId);
		assertThat(redeemed.state()).isEqualTo(PairingState.PENDING);
	}

	@Test
	void redeemByLinkRejectsAnUnknownCode() {
		assertThatThrownBy(() -> serviceAt(NOW).redeemByLink("codigo-que-nao-existe", inviteeId))
				.isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemByLinkRejectsACodeAlreadyUsed() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		IssuedPairingInvite invite = service.inviteByLink(initiatorId);
		service.redeemByLink(invite.code(), inviteeId);
		Long anotherPerson = users.save(new User("sub-cp-d", "cp-d@example.com", "D")).getId();

		assertThatThrownBy(() -> service.redeemByLink(invite.code(), anotherPerson))
				.isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemByLinkRejectsAnExpiredCode() {
		IssuedPairingInvite invite = serviceAt(NOW).inviteByLink(initiatorId);
		Instant afterExpiry = NOW.plus(CouplePairingServiceImpl.TTL).plusSeconds(1);

		assertThatThrownBy(() -> serviceAt(afterExpiry).redeemByLink(invite.code(), inviteeId))
				.isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemByLinkRejectsTheInitiatorUsingTheirOwnCode() {
		IssuedPairingInvite invite = serviceAt(NOW).inviteByLink(initiatorId);

		assertThatThrownBy(() -> serviceAt(NOW).redeemByLink(invite.code(), initiatorId))
				.isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void inviteByHandleFindsTheInviteeByHandleIgnoringCase() {
		giveHandle(inviteeId, "handle_b");

		PairingSummary pairing = serviceAt(NOW).inviteByHandle(initiatorId, "Handle_B");

		assertThat(pairing.inviteeUserId()).isEqualTo(inviteeId);
		assertThat(pairing.channel()).isEqualTo(Channel.HANDLE);
		assertThat(pairing.state()).isEqualTo(PairingState.PENDING);
	}

	@Test
	void inviteByHandleRejectsAnUnknownHandle() {
		assertThatThrownBy(() -> serviceAt(NOW).inviteByHandle(initiatorId, "ninguem_tem_esse"))
				.isInstanceOf(HandleNotInvitableException.class);
	}

	@Test
	void inviteByHandleRejectsTheInitiatorsOwnHandle() {
		giveHandle(initiatorId, "handle_a");

		assertThatThrownBy(() -> serviceAt(NOW).inviteByHandle(initiatorId, "handle_a"))
				.isInstanceOf(HandleNotInvitableException.class);
	}

	@Test
	void inviteByHandleRejectsSomeoneAlreadyInARelationship() {
		giveHandle(inviteeId, "handle_b");
		Couple couple = couples.save(new Couple(CoupleState.ACTIVE));
		coupleMembers.save(new CoupleMember(couple.getId(), inviteeId));

		assertThatThrownBy(() -> serviceAt(NOW).inviteByHandle(initiatorId, "handle_b"))
				.isInstanceOf(HandleNotInvitableException.class);
	}

	@Test
	void inviteByHandleRespectsTheRateLimiter() {
		giveHandle(inviteeId, "handle_b");

		assertThatThrownBy(() -> serviceAt(NOW, alwaysBlock()).inviteByHandle(initiatorId, "handle_b"))
				.isInstanceOf(RateLimitExceededException.class);
	}

	private void writeFullStory(CouplePairingServiceImpl service, Long pairingId, Long userId) {
		service.saveStoryDraft(pairingId, userId, StoryStep.HOW_WE_MET, "Nos conhecemos numa festa.");
		service.saveStoryDraft(pairingId, userId, StoryStep.TIME_BEFORE_DATING, "Conversamos por meses.");
		service.saveStoryDraft(pairingId, userId, StoryStep.WHAT_I_THOUGHT_THEN, "Achei engraçada.");
		service.saveStoryDraft(pairingId, userId, StoryStep.WHAT_I_THINK_NOW, "Acho ainda mais hoje.");
	}

	@Test
	void savingAStoryDraftRequiresTheBondToBeFirmedFirst() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		assertThatThrownBy(
				() -> service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.HOW_WE_MET, "Nos conhecemos."))
				.isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void savingAStoryDraftNeverRequiresAllFourStepsAtOnce() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);

		service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.HOW_WE_MET, "Nos conhecemos numa festa.");

		assertThat(service.isStoryComplete(pairing.id(), initiatorId)).isFalse();
	}

	@Test
	void savingAStoryDraftDoesNotEnforceAMinimumLength() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);

		service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.HOW_WE_MET, "oi");
		service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.TIME_BEFORE_DATING, "oi");
		service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.WHAT_I_THOUGHT_THEN, "oi");
		service.saveStoryDraft(pairing.id(), initiatorId, StoryStep.WHAT_I_THINK_NOW, "oi");

		assertThat(service.isStoryComplete(pairing.id(), initiatorId)).isTrue();
	}

	@Test
	void someoneOutsideThePairingCannotSaveOrReadAStoryInIt() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Long outsider = users.save(new User("sub-cp-e", "cp-e@example.com", "E")).getId();

		assertThatThrownBy(() -> service.saveStoryDraft(pairing.id(), outsider, StoryStep.HOW_WE_MET, "oi"))
				.isInstanceOf(PairingNotFoundException.class);
		assertThatThrownBy(() -> service.isStoryComplete(pairing.id(), outsider))
				.isInstanceOf(PairingNotFoundException.class);
	}

	@Test
	void requireStoryCompleteThrowsWhileAStepIsStillEmpty() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		assertThatThrownBy(() -> service.requireStoryComplete(pairing.id(), initiatorId))
				.isInstanceOf(StoryIncompleteException.class);
	}

	@Test
	void requireStoryCompleteDoesNotThrowOnceAllFourStepsHaveText() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);
		writeFullStory(service, pairing.id(), initiatorId);

		service.requireStoryComplete(pairing.id(), initiatorId);
	}

	@Test
	void readPartnerStoryStaysEmptyWhileOnlyOneSideIsComplete() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);
		writeFullStory(service, pairing.id(), initiatorId);

		assertThat(service.readPartnerStory(pairing.id(), inviteeId)).isEmpty();
	}

	@Test
	void readPartnerStoryRevealsBothSidesOnceBothAreComplete() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);
		writeFullStory(service, pairing.id(), initiatorId);
		writeFullStory(service, pairing.id(), inviteeId);

		Optional<StorySummary> seenByInvitee = service.readPartnerStory(pairing.id(), inviteeId);
		Optional<StorySummary> seenByInitiator = service.readPartnerStory(pairing.id(), initiatorId);

		assertThat(seenByInvitee).isPresent();
		assertThat(seenByInvitee.get().userId()).isEqualTo(initiatorId);
		assertThat(seenByInitiator).isPresent();
		assertThat(seenByInitiator.get().userId()).isEqualTo(inviteeId);
	}

	@Test
	void readPartnerStoryIsEmptyForALinkInviteThatNobodyRedeemedYet() {
		// Sem convidado, o pareamento nunca fica CONFIRMED (isFullyConfirmed exige os dois lados) — então
		// nem dá pra escrever a própria história ainda; só resta confirmar que não existe parceiro nenhum.
		CouplePairingServiceImpl service = serviceAt(NOW);
		IssuedPairingInvite invite = service.inviteByLink(initiatorId);

		assertThat(service.readPartnerStory(invite.pairingId(), initiatorId)).isEmpty();
	}

	private static final LocalDate KNOWN_SINCE = LocalDate.of(2023, 5, 1);
	private static final LocalDate DATING_SINCE = LocalDate.of(2023, 8, 12);

	@Test
	void proposingDatesRequiresTheBondToBeFirmedFirst() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		assertThatThrownBy(() -> service.proposeDates(pairing.id(), initiatorId, KNOWN_SINCE, DATING_SINCE))
				.isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void datesAreNotAcceptedUntilTheOtherPersonAccepts() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);

		service.proposeDates(pairing.id(), initiatorId, KNOWN_SINCE, DATING_SINCE);

		assertThat(service.areDatesAccepted(pairing.id(), initiatorId)).isFalse();
		assertThatThrownBy(() -> service.requireDatesAccepted(pairing.id(), initiatorId))
				.isInstanceOf(DatesNotAcceptedException.class);
	}

	@Test
	void theOtherPersonAcceptingTheProposalSatisfiesRequireDatesAccepted() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);
		service.proposeDates(pairing.id(), initiatorId, KNOWN_SINCE, DATING_SINCE);

		service.acceptDates(pairing.id(), inviteeId);

		assertThat(service.areDatesAccepted(pairing.id(), initiatorId)).isTrue();
		service.requireDatesAccepted(pairing.id(), initiatorId);
	}

	@Test
	void acceptingDatesCopiesThemOntoTheAlreadyBornCouple() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);
		service.proposeDates(pairing.id(), initiatorId, KNOWN_SINCE, DATING_SINCE);

		service.acceptDates(pairing.id(), inviteeId);

		Long coupleId = pairings.findById(pairing.id()).orElseThrow().getCoupleId();
		Couple couple = couples.findById(coupleId).orElseThrow();
		assertThat(couple.getKnownSinceDate()).isEqualTo(KNOWN_SINCE);
		assertThat(couple.getDatingSinceDate()).isEqualTo(DATING_SINCE);
	}

	@Test
	void someoneOutsideThePairingCannotProposeOrAcceptDates() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Long outsider = users.save(new User("sub-cp-f", "cp-f@example.com", "F")).getId();

		assertThatThrownBy(() -> service.proposeDates(pairing.id(), outsider, KNOWN_SINCE, DATING_SINCE))
				.isInstanceOf(PairingNotFoundException.class);
		assertThatThrownBy(() -> service.acceptDates(pairing.id(), outsider))
				.isInstanceOf(PairingNotFoundException.class);
	}

	// Os testes "cancelar/expirar apaga a história" (RN-29, Etapa 12) não são mais alcançáveis pela API
	// pública: escrever história exige CONFIRMED, e cancelar/expirar só acontece a partir de PENDING — os
	// dois nunca coexistem no mesmo pareamento. O código de limpeza (cancelAndEraseDrafts) continua ali,
	// defensivo e inofensivo, mesma situação da proposta de datas (ver CouplePairing.cancel).

	@Test
	void confirmRecordsOneSideWithoutBirthingTheCoupleYet() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);

		PairingSummary afterOneConfirmation = service.confirm(pairing.id(), initiatorId);

		assertThat(afterOneConfirmation.state()).isEqualTo(PairingState.PENDING);
		assertThat(pairings.findById(pairing.id()).orElseThrow().getCoupleId()).isNull();
	}

	@Test
	void confirmBirthsAnEmptyCoupleOnceBothSidesHaveConfirmed() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		service.confirm(pairing.id(), initiatorId);

		PairingSummary afterBothConfirmed = service.confirm(pairing.id(), inviteeId);

		assertThat(afterBothConfirmed.state()).isEqualTo(PairingState.CONFIRMED);
		Long coupleId = pairings.findById(pairing.id()).orElseThrow().getCoupleId();
		assertThat(coupleId).isNotNull();
		Couple couple = couples.findById(coupleId).orElseThrow();
		assertThat(couple.getState()).isEqualTo(CoupleState.ACTIVE);
		assertThat(couple.getKnownSinceDate()).isNull();
		assertThat(coupleMembers.findByCoupleIdAndLeftAtIsNull(coupleId)).hasSize(2);
	}

	@Test
	void confirmOnAnExpiredPairingCancelsItInsteadOfConfirming() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Instant afterExpiry = NOW.plus(CouplePairingServiceImpl.TTL).plusSeconds(1);

		PairingSummary result = serviceAt(afterExpiry).confirm(pairing.id(), initiatorId);

		assertThat(result.state()).isEqualTo(PairingState.CANCELLED);
	}

	@Test
	void someoneOutsideThePairingCannotConfirmIt() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		Long outsider = users.save(new User("sub-cp-h", "cp-h@example.com", "H")).getId();

		assertThatThrownBy(() -> service.confirm(pairing.id(), outsider)).isInstanceOf(PairingNotFoundException.class);
	}

	@Test
	void confirmingAgainOnAnAlreadyConfirmedPairingIsAnIllegalTransition() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		PairingSummary pairing = confirmedPairing(service);

		assertThatThrownBy(() -> service.confirm(pairing.id(), initiatorId))
				.isInstanceOf(com.duo.relationships.exception.IllegalPairingTransitionException.class);
	}

	/**
	 * RN-06, a segunda corrida: a mesma pessoa confirma dois pareamentos diferentes. O índice único do banco
	 * (desde a Etapa 1) recusa o segundo CoupleMember; o {@code REQUIRES_NEW} do CoupleFactory garante que só
	 * o nascimento do segundo Couple desfaz — a confirmação em si (gravada por fora) continua valendo.
	 */
	@Test
	void confirmingASecondPairingForSomeoneAlreadyInARelationshipFailsButKeepsTheConfirmation() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		Long otherPartner = users.save(new User("sub-cp-i", "cp-i@example.com", "I")).getId();
		PairingSummary firstPairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		service.confirm(firstPairing.id(), initiatorId);
		service.confirm(firstPairing.id(), inviteeId);
		PairingSummary secondPairing = service.initiate(initiatorId, otherPartner, Channel.HANDLE);
		service.confirm(secondPairing.id(), initiatorId);

		assertThatThrownBy(() -> service.confirm(secondPairing.id(), otherPartner))
				.isInstanceOf(AlreadyInRelationshipException.class);

		CouplePairing stored = pairings.findById(secondPairing.id()).orElseThrow();
		assertThat(stored.getInitiatorConfirmedAt()).isNotNull();
		assertThat(stored.getInviteeConfirmedAt()).isNotNull();
		assertThat(stored.isFullyConfirmed()).isTrue();
		assertThat(stored.getState()).isEqualTo(PairingState.PENDING);
	}

	@Test
	void aPersonCanHaveMoreThanOnePendingPairingAtTheSameTime() {
		CouplePairingServiceImpl service = serviceAt(NOW);
		Long anotherInvitee = users.save(new User("sub-cp-g", "cp-g@example.com", "G")).getId();

		PairingSummary first = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		PairingSummary second = service.initiate(initiatorId, anotherInvitee, Channel.HANDLE);

		assertThat(service.find(first.id(), initiatorId).state()).isEqualTo(PairingState.PENDING);
		assertThat(service.find(second.id(), initiatorId).state()).isEqualTo(PairingState.PENDING);
	}
}
