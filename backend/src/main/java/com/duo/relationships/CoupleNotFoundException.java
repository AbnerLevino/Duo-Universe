package com.duo.relationships;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Mensagem genérica de propósito: não revela se o casal simplesmente não existe ou se existe, mas a pessoa
 * não é (ou não é mais) membro ativo dele (RN-12/RN-20, mesmo espírito do `PairingNotFoundException`). */
public class CoupleNotFoundException extends DuoHttpException {

	public CoupleNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Casal não encontrado.");
	}
}
