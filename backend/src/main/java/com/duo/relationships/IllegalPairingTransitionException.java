package com.duo.relationships;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** RN-08: o pareamento só sai de PENDING uma vez — para CONFIRMED (Etapa 8) ou CANCELLED (recusa/expira/cancela). */
public class IllegalPairingTransitionException extends DuoHttpException {

	public IllegalPairingTransitionException(PairingState from, PairingState to) {
		super(HttpStatus.CONFLICT, "Não é possível ir de " + from + " para " + to + ": o pareamento já foi decidido.");
	}
}
