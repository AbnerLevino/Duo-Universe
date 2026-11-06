package com.duo.relationships.internal.service;

import com.duo.relationships.CoupleState;
import com.duo.relationships.exception.AlreadyInRelationshipException;
import com.duo.relationships.internal.model.Couple;
import com.duo.relationships.internal.model.CoupleMember;
import com.duo.relationships.internal.repository.CoupleMemberRepository;
import com.duo.relationships.internal.repository.CoupleRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o {@code Couple} (vazio — sem história, sem datas ainda) e os dois {@code CoupleMember} quando um
 * pareamento é totalmente confirmado (RN-06/RN-08). Roda numa transação própria e independente
 * ({@code REQUIRES_NEW}, não a mesma de quem chama): se o índice único de RN-06 recusar porque uma das duas
 * pessoas já está em outro vínculo, só esta transação de dentro desfaz — a confirmação que
 * {@code CouplePairingServiceImpl.confirm} já tinha gravado por fora continua de pé.
 *
 * <p>Precisa ser uma classe (um <em>bean</em>) separada: o Spring só aplica {@code @Transactional} através de
 * um proxy em volta do bean, e uma chamada de um método para outro <strong>dentro da mesma classe</strong>
 * não passa por esse proxy — a anotação seria ignorada.
 */
@Component
class CoupleFactory {

	private final CoupleRepository couples;
	private final CoupleMemberRepository members;

	CoupleFactory(CoupleRepository couples, CoupleMemberRepository members) {
		this.couples = couples;
		this.members = members;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long createFrom(Long initiatorUserId, Long inviteeUserId) {
		try {
			Couple couple = couples.save(new Couple(CoupleState.ACTIVE));
			members.save(new CoupleMember(couple.getId(), initiatorUserId));
			members.saveAndFlush(new CoupleMember(couple.getId(), inviteeUserId));
			return couple.getId();
		}
		catch (DataIntegrityViolationException raceOrConflict) {
			throw new AlreadyInRelationshipException();
		}
	}
}
