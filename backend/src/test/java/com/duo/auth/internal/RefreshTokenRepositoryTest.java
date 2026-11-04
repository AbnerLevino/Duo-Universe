package com.duo.auth.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.duo.users.internal.User;
import com.duo.users.internal.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryTest {

	@Autowired
	RefreshTokenRepository refreshTokens;

	@Autowired
	UserRepository users;

	private Long newUserId(String suffix) {
		return users.save(new User("sub-rt-" + suffix, "rt-" + suffix + "@example.com", "RT " + suffix)).getId();
	}

	private RefreshToken token(Long userId, String hash, UUID family) {
		return new RefreshToken(userId, hash, family, Instant.now().plus(30, ChronoUnit.DAYS));
	}

	@Test
	void savesAndFindsByHash() {
		Long userId = newUserId("a");
		refreshTokens.save(token(userId, "hash-a", UUID.randomUUID()));

		assertThat(refreshTokens.findByTokenHash("hash-a")).isPresent();
		assertThat(refreshTokens.findByTokenHash("outro")).isEmpty();
	}

	@Test
	void rejectsDuplicateHash() {
		Long userId = newUserId("b");
		refreshTokens.saveAndFlush(token(userId, "hash-b", UUID.randomUUID()));

		assertThatThrownBy(() -> refreshTokens.saveAndFlush(token(userId, "hash-b", UUID.randomUUID())))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void revokeFamilyOnlyTouchesThatFamily() {
		Long userId = newUserId("c");
		UUID familyA = UUID.randomUUID();
		UUID familyB = UUID.randomUUID();
		refreshTokens.save(token(userId, "hash-c1", familyA));
		refreshTokens.save(token(userId, "hash-c2", familyA));
		refreshTokens.save(token(userId, "hash-c3", familyB));
		refreshTokens.flush();

		int revoked = refreshTokens.revokeFamily(familyA, Instant.now());

		assertThat(revoked).isEqualTo(2);
	}

	@Test
	void revokeAllOfUserTouchesOnlyActiveTokens() {
		Long userId = newUserId("d");
		RefreshToken already = token(userId, "hash-d1", UUID.randomUUID());
		already.revoke(Instant.now(), null);
		refreshTokens.save(already);
		refreshTokens.save(token(userId, "hash-d2", UUID.randomUUID()));
		refreshTokens.flush();

		int revoked = refreshTokens.revokeAllOfUser(userId, Instant.now());

		assertThat(revoked).isEqualTo(1);
	}
}
