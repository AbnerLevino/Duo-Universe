package com.duo.relationships.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {

	/** RN-06: a pessoa só tem uma linha "ativa" (left_at nulo) por vez — garantido pelo índice único da migration. */
	Optional<CoupleMember> findByUserIdAndLeftAtIsNull(Long userId);
}
