package com.duo.relationships.internal.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import com.duo.relationships.PairingState;
import com.duo.relationships.internal.model.CouplePairing;

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

	/** Etapa 13: o que o app mostra ao abrir — os convites ainda em aberto da pessoa, de qualquer lado. */
	@Query("select p from CouplePairing p where p.state = :state "
			+ "and (p.initiatorUserId = :userId or p.inviteeUserId = :userId)")
	List<CouplePairing> findByUserIdAndState(@Param("userId") Long userId, @Param("state") PairingState state);
}
