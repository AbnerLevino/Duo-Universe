package com.duo.auth.internal.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.duo.auth.internal.model.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	/** Trava a linha até o fim da transação: duas rotações simultâneas do mesmo token não passam juntas. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from RefreshToken t where t.tokenHash = :tokenHash")
	Optional<RefreshToken> findForUpdateByTokenHash(String tokenHash);

	/** Encerra toda a família de uma vez (RA-05: reuso detectado) sem mexer nos já revogados. */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :when where t.familyId = :familyId and t.revokedAt is null")
	int revokeFamily(UUID familyId, Instant when);

	/** "Sair de todos os aparelhos" (H4). */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :when where t.userId = :userId and t.revokedAt is null")
	int revokeAllOfUser(Long userId, Instant when);
}
