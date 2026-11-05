package com.duo.shared.internal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.duo.shared.RateLimitExceededException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/** RA-08 contra o banco de verdade; o serviço é montado à mão para controlar o relógio (mesmo padrão de
 * RefreshTokenServiceImplTest). */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RateLimiterImplTest {

	private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

	private static final Duration WINDOW = Duration.ofHours(1);

	@Autowired
	RateLimitAttemptRepository attempts;

	private RateLimiterImpl limiterAt(Instant now) {
		return new RateLimiterImpl(attempts, Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void allowsAttemptsUpToTheMax() {
		RateLimiterImpl limiter = limiterAt(NOW);

		assertThatCode(() -> {
			for (int i = 0; i < 3; i++) {
				limiter.checkAndRecord("scope-a", "key-1", 3, WINDOW);
			}
		}).doesNotThrowAnyException();
	}

	@Test
	void blocksOnceTheMaxIsExceeded() {
		RateLimiterImpl limiter = limiterAt(NOW);
		for (int i = 0; i < 3; i++) {
			limiter.checkAndRecord("scope-b", "key-1", 3, WINDOW);
		}

		assertThatThrownBy(() -> limiter.checkAndRecord("scope-b", "key-1", 3, WINDOW))
				.isInstanceOf(RateLimitExceededException.class);
	}

	@Test
	void resetsOnceTheWindowExpires() {
		RateLimiterImpl limiter = limiterAt(NOW);
		for (int i = 0; i < 3; i++) {
			limiter.checkAndRecord("scope-c", "key-1", 3, WINDOW);
		}
		Instant afterWindow = NOW.plus(WINDOW).plusSeconds(1);

		assertThatCode(() -> limiterAt(afterWindow).checkAndRecord("scope-c", "key-1", 3, WINDOW))
				.doesNotThrowAnyException();
	}

	@Test
	void differentKeysDoNotShareTheCounter() {
		RateLimiterImpl limiter = limiterAt(NOW);
		for (int i = 0; i < 3; i++) {
			limiter.checkAndRecord("scope-d", "key-1", 3, WINDOW);
		}

		assertThatCode(() -> limiter.checkAndRecord("scope-d", "key-2", 3, WINDOW)).doesNotThrowAnyException();
	}

	@Test
	void differentScopesDoNotShareTheCounterEvenForTheSameKey() {
		RateLimiterImpl limiter = limiterAt(NOW);
		for (int i = 0; i < 3; i++) {
			limiter.checkAndRecord("scope-e1", "key-1", 3, WINDOW);
		}

		assertThatCode(() -> limiter.checkAndRecord("scope-e2", "key-1", 3, WINDOW)).doesNotThrowAnyException();
	}
}
