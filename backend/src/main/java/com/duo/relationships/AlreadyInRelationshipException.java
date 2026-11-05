package com.duo.relationships;

/** RN-06: a pessoa já está em outro vínculo ACTIVE/PAUSED no exato momento em que o Couple tentaria nascer.
 * A confirmação dela neste pareamento continua registrada — só o nascimento do Couple falhou (ver
 * CouplePairingServiceImpl.confirm e CoupleFactory). */
public class AlreadyInRelationshipException extends RuntimeException {

	public AlreadyInRelationshipException() {
		super("Uma das duas pessoas já está em outro vínculo ativo.");
	}
}
