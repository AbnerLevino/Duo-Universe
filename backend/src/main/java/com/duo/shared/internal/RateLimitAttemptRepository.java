package com.duo.shared.internal;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RateLimitAttemptRepository extends JpaRepository<RateLimitAttempt, Long> {

	/** Trava a linha até o fim da transação: duas tentativas simultâneas da mesma chave não passam juntas. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from RateLimitAttempt a where a.scope = :scope and a.attemptKey = :attemptKey")
	Optional<RateLimitAttempt> findForUpdateByScopeAndAttemptKey(String scope, String attemptKey);
}
