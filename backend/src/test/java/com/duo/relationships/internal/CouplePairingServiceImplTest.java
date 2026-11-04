package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.duo.relationships.Channel;
import com.duo.relationships.PairingNotFoundException;
import com.duo.relationships.PairingState;
import com.duo.relationships.PairingSummary;
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
	UserRepository users;

	Long initiatorId;
	Long inviteeId;

	@BeforeEach
	void createUsers() {
		initiatorId = users.save(new User("sub-cp-a", "cp-a@example.com", "A")).getId();
		inviteeId = users.save(new User("sub-cp-b", "cp-b@example.com", "B")).getId();
	}

	private CouplePairingServiceImpl serviceAt(Instant now) {
		return new CouplePairingServiceImpl(pairings, Clock.fixed(now, ZoneOffset.UTC));
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
}
