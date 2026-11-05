package com.duo.relationships;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** RN-06: a pessoa já está em outro vínculo ACTIVE/PAUSED no exato momento em que o Couple tentaria nascer.
 * A confirmação dela neste pareamento continua registrada — só o nascimento do Couple falhou (ver
 * CouplePairingServiceImpl.confirm e CoupleFactory). */
public class AlreadyInRelationshipException extends DuoHttpException {

	public AlreadyInRelationshipException() {
		super(HttpStatus.CONFLICT, "Uma das duas pessoas já está em outro vínculo ativo.");
	}
}
