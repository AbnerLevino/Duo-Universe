package com.duo.relationships;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** A máquina de estados do {@code Couple} (ver a matriz em `docs/epico-2-vinculo.md`): só ACTIVE ⇄ PAUSED, e
 * de qualquer um dos dois para ENDED (final, RN-18). */
public class IllegalCoupleTransitionException extends DuoHttpException {

	public IllegalCoupleTransitionException(CoupleState from, CoupleState to) {
		super(HttpStatus.CONFLICT, "Não é possível ir de " + from + " para " + to + ".");
	}
}
