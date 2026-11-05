package com.duo.relationships.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouplePairingStoryRepository extends JpaRepository<CouplePairingStory, Long> {

	Optional<CouplePairingStory> findByPairingIdAndUserId(Long pairingId, Long userId);
}
