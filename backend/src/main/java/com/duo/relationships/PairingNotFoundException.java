package com.duo.relationships;

/** Mensagem genérica de propósito: não revela se o pareamento simplesmente não existe ou se existe, mas não
 * pertence a quem pediu (RN-30, no mesmo espírito do RA-07). */
public class PairingNotFoundException extends RuntimeException {

	public PairingNotFoundException() {
		super("Pareamento não encontrado.");
	}
}
