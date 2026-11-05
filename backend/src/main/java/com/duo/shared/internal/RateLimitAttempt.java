package com.duo.shared.internal;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Uma linha por (scope, attemptKey): quantas tentativas essa chave fez dentro da janela atual (RA-08). */
@Entity
@Table(name = "rate_limit_attempts")
public class RateLimitAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String scope;

	@Column(name = "attempt_key", nullable = false, length = 255)
	private String attemptKey;

	@Column(name = "window_started_at", nullable = false)
	private Instant windowStartedAt;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	protected RateLimitAttempt() {
	}

	public RateLimitAttempt(String scope, String attemptKey, Instant windowStartedAt) {
		this.scope = scope;
		this.attemptKey = attemptKey;
		this.windowStartedAt = windowStartedAt;
		this.attemptCount = 1;
	}

	public String getScope() {
		return scope;
	}

	public String getAttemptKey() {
		return attemptKey;
	}

	public Instant getWindowStartedAt() {
		return windowStartedAt;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public boolean windowExpired(Instant now, Duration window) {
		return now.isAfter(windowStartedAt.plus(window));
	}

	public void resetWindow(Instant now) {
		this.windowStartedAt = now;
		this.attemptCount = 1;
	}

	public void increment() {
		this.attemptCount++;
	}
}
