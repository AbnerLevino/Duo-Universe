package com.duo.relationships.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouplePairingRepository extends JpaRepository<CouplePairing, Long> {

	Optional<CouplePairing> findByInviteCodeHash(String inviteCodeHash);
}
