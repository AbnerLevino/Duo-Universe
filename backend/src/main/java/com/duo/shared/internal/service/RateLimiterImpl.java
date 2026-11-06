package com.duo.shared.internal.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.duo.shared.exception.RateLimitExceededException;
import com.duo.shared.internal.model.RateLimitAttempt;
import com.duo.shared.internal.repository.RateLimitAttemptRepository;
import com.duo.shared.RateLimiter;

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
		RateLimitAttempt attempt = attempts.findForUpdateByScopeAndAttemptKey(scope, key).orElse(null);
		if (attempt == null) {
			attempts.save(new RateLimitAttempt(scope, key, now));
			return;
		}
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
