package com.duo.shared.internal.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.duo.shared.RateLimiter;
import com.duo.shared.exception.RateLimitExceededException;
import com.duo.shared.internal.model.RateLimitAttempt;
import com.duo.shared.internal.repository.RateLimitAttemptRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RateLimiterImpl implements RateLimiter {

	private final RateLimitAttemptRepository attempts;
	private final Clock clock;

	RateLimiterImpl(RateLimitAttemptRepository attempts, Clock clock) {
		this.attempts = attempts;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void checkAndRecord(String scope, String key, int maxAttempts, Duration window) {
		Instant now = clock.instant();
		// RF-A06: garante a linha (sem erro se duas requisições chegarem juntas) e só então a trava.
		attempts.insertIfAbsent(scope, key, now);
		RateLimitAttempt attempt = attempts.findForUpdateByScopeAndAttemptKey(scope, key).orElseThrow();
		if (attempt.windowExpired(now, window)) {
			attempt.resetWindow(now);
			return;
		}
		if (attempt.getAttemptCount() >= maxAttempts) {
			throw new RateLimitExceededException();
		}
		attempt.increment();
	}
}
