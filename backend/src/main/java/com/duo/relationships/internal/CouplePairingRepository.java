package com.duo.relationships.internal;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouplePairingRepository extends JpaRepository<CouplePairing, Long> {

	Optional<CouplePairing> findByInviteCodeHash(String inviteCodeHash);

	/** Trava a linha até o fim da transação: as duas confirmações do mesmo pareamento não passam juntas
	 * (RN-08, Etapa 8) — mesmo truque de {@code RefreshTokenRepository.findForUpdateByTokenHash}. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from CouplePairing p where p.id = :id")
	Optional<CouplePairing> findByIdForUpdate(@Param("id") Long id);
}
