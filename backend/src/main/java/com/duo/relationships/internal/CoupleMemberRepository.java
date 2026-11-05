package com.duo.relationships.internal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {

	/** RN-06: a pessoa só tem uma linha "ativa" (left_at nulo) por vez — garantido pelo índice único da migration. */
	Optional<CoupleMember> findByUserIdAndLeftAtIsNull(Long userId);

	Optional<CoupleMember> findByCoupleIdAndUserIdAndLeftAtIsNull(Long coupleId, Long userId);

	/** Usado pela Etapa 10 (RN-14): quem ainda está "dentro" do casal, para encerrar a filiação de todos
	 * quando alguém sai (no MVP, 2 membros — sair de um termina o vínculo inteiro). */
	List<CoupleMember> findByCoupleIdAndLeftAtIsNull(Long coupleId);
}
