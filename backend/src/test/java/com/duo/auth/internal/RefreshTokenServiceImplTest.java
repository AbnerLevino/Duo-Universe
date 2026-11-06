package com.duo.auth.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.duo.auth.IssuedRefreshToken;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/** RA-04 e RA-05 contra o banco de verdade; o serviço é montado à mão para controlar o relógio. */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

	@Autowired
	RefreshTokenRepository tokens;

	@Autowired
	UserRepository users;

	Long userId;

	@BeforeEach
	void createUser() {
		userId = users.save(new User("sub-svc", "svc@example.com", "Svc")).getId();
	}

	private RefreshTokenServiceImpl serviceAt(Instant now) {
		return new RefreshTokenServiceImpl(tokens, Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void storesOnlyTheHashNeverTheToken() {
		IssuedRefreshToken issued = serviceAt(NOW).issue(userId);

		assertThat(tokens.findByTokenHash(issued.token())).isEmpty();
		assertThat(tokens.findByTokenHash(RefreshTokenServiceImpl.hash(issued.token()))).isPresent();
		assertThat(issued.expiresAt()).isEqualTo(NOW.plus(RefreshTokenServiceImpl.TTL));
	}

	@Test
	void rotatesIntoANewTokenOfTheSameFamilyAndRevokesTheOldOne() {
		RefreshTokenServiceImpl service = serviceAt(NOW);
		IssuedRefreshToken first = service.issue(userId);

		IssuedRefreshToken second = service.rotate(first.token()).orElseThrow();

		RefreshToken oldRow = tokens.findByTokenHash(RefreshTokenServiceImpl.hash(first.token())).orElseThrow();
		RefreshToken newRow = tokens.findByTokenHash(RefreshTokenServiceImpl.hash(second.token())).orElseThrow();
		assertThat(second.token()).isNotEqualTo(first.token());
		assertThat(oldRow.isRevoked()).isTrue();
		assertThat(oldRow.getReplacedBy()).isEqualTo(newRow.getId());
		assertThat(newRow.getFamilyId()).isEqualTo(oldRow.getFamilyId());
		assertThat(newRow.isRevoked()).isFalse();
	}

	@Test
	void reusingAnOldTokenRevokesTheWholeFamily() {
		RefreshTokenServiceImpl service = serviceAt(NOW);
		IssuedRefreshToken first = service.issue(userId);
		IssuedRefreshToken second = service.rotate(first.token()).orElseThrow();

		assertThat(service.rotate(first.token())).isEmpty();

		RefreshToken newestRow = tokens.findByTokenHash(RefreshTokenServiceImpl.hash(second.token())).orElseThrow();
		assertThat(newestRow.isRevoked()).isTrue();
		assertThat(service.rotate(second.token())).isEmpty();
	}

	@Test
	void reuseDoesNotAffectOtherFamilies() {
		RefreshTokenServiceImpl service = serviceAt(NOW);
		IssuedRefreshToken phone = service.issue(userId);
		IssuedRefreshToken notebook = service.issue(userId);
		service.rotate(phone.token());

		service.rotate(phone.token());

		assertThat(service.rotate(notebook.token())).isPresent();
	}

	@Test
	void rejectsUnknownToken() {
		assertThat(serviceAt(NOW).rotate("token-que-nao-existe")).isEmpty();
	}

	@Test
	void rejectsExpiredToken() {
		IssuedRefreshToken issued = serviceAt(NOW).issue(userId);
		Instant afterExpiry = NOW.plus(RefreshTokenServiceImpl.TTL).plus(Duration.ofSeconds(1));

		assertThat(serviceAt(afterExpiry).rotate(issued.token())).isEmpty();
	}

	@Test
	void revokeEndsOnlyThatDeviceFamily() {
		RefreshTokenServiceImpl service = serviceAt(NOW);
		IssuedRefreshToken phone = service.issue(userId);
		IssuedRefreshToken notebook = service.issue(userId);

		service.revoke(phone.token());

		assertThat(service.rotate(phone.token())).isEmpty();
		assertThat(service.rotate(notebook.token())).isPresent();
	}

	@Test
	void revokeAllEndsEveryDevice() {
		RefreshTokenServiceImpl service = serviceAt(NOW);
		IssuedRefreshToken phone = service.issue(userId);
		IssuedRefreshToken notebook = service.issue(userId);

		service.revokeAll(userId);

		assertThat(service.rotate(phone.token())).isEmpty();
		assertThat(service.rotate(notebook.token())).isEmpty();
	}
}
