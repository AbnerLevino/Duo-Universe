package com.duo.relationships;

/** Mensagem genérica de propósito: não revela se o casal simplesmente não existe ou se existe, mas a pessoa
 * não é (ou não é mais) membro ativo dele (RN-12/RN-20, mesmo espírito do `PairingNotFoundException`). */
public class CoupleNotFoundException extends RuntimeException {

	public CoupleNotFoundException() {
		super("Casal não encontrado.");
	}
}
