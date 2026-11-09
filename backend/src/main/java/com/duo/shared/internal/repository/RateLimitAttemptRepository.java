package com.duo.shared.internal.repository;

import java.time.Instant;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import com.duo.shared.internal.model.RateLimitAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RateLimitAttemptRepository extends JpaRepository<RateLimitAttempt, Long> {

	/**
	 * RF-A06: cria a linha da chave se ainda não existe, sem erro se outra requisição criou primeiro
	 * ({@code ON CONFLICT DO NOTHING}). Antes, duas primeiras tentativas simultâneas batiam no índice único e uma
	 * virava erro 500.
	 */
	@Modifying
	@Query(nativeQuery = true, value = "insert into rate_limit_attempts (scope, attempt_key, window_started_at, attempt_count) "
			+ "values (:scope, :attemptKey, :now, 0) on conflict (scope, attempt_key) do nothing")
	void insertIfAbsent(String scope, String attemptKey, Instant now);

	/** Trava a linha até o fim da transação: duas tentativas simultâneas da mesma chave não passam juntas. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from RateLimitAttempt a where a.scope = :scope and a.attemptKey = :attemptKey")
	Optional<RateLimitAttempt> findForUpdateByScopeAndAttemptKey(String scope, String attemptKey);
}
