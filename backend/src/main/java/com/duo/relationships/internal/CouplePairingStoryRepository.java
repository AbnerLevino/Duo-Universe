package com.duo.relationships.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouplePairingStoryRepository extends JpaRepository<CouplePairingStory, Long> {

	Optional<CouplePairingStory> findByPairingIdAndUserId(Long pairingId, Long userId);

	/** RN-29: pareamento cancelado, recusado ou vencido não guarda rascunho de história nenhum. */
	void deleteAllByPairingId(Long pairingId);
}
