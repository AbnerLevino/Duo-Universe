package com.duo.relationships;

/** RN-08: o pareamento só sai de PENDING uma vez — para CONFIRMED (Etapa 8) ou CANCELLED (recusa/expira/cancela). */
public class IllegalPairingTransitionException extends RuntimeException {

	public IllegalPairingTransitionException(PairingState from, PairingState to) {
		super("Não é possível ir de " + from + " para " + to + ": o pareamento já foi decidido.");
	}
}
