package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.duo.relationships.Channel;
import com.duo.relationships.HandleNotInvitableException;
import com.duo.relationships.InviteCodeInvalidException;
import com.duo.relationships.IssuedPairingInvite;
import com.duo.relationships.PairingNotFoundException;
import com.duo.relationships.PairingState;
import com.duo.relationships.PairingSummary;
import com.duo.shared.RateLimitExceededException;
import com.duo.shared.RateLimiter;
import com.duo.users.UserService;
import com.duo.users.UserSummary;
import com.duo.users.internal.User;
import com.duo.users.internal.UserRepository;

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
		return new CouplePairingServiceImpl(
				pairings, coupleMembers, fakeUserService(), rateLimiter, Clock.fixed(now, ZoneOffset.UTC));
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
		Couple couple = couples.save(new Couple(Couple.State.ACTIVE));
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
}
