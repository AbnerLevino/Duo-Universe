package com.duo.relationships;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Mensagem genérica de propósito: não revela se o pareamento simplesmente não existe ou se existe, mas não
 * pertence a quem pediu (RN-30, no mesmo espírito do RA-07). */
public class PairingNotFoundException extends DuoHttpException {

	public PairingNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Pareamento não encontrado.");
	}
}
