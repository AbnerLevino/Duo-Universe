package com.duo.relationships;

/** A máquina de estados do {@code Couple} (ver a matriz em `docs/epico-2-vinculo.md`): só ACTIVE ⇄ PAUSED, e
 * de qualquer um dos dois para ENDED (final, RN-18). */
public class IllegalCoupleTransitionException extends RuntimeException {

	public IllegalCoupleTransitionException(CoupleState from, CoupleState to) {
		super("Não é possível ir de " + from + " para " + to + ".");
	}
}
