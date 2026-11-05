package com.duo.relationships;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Uma ação tentada fora da hora certa do fluxo do vínculo — ex.: escrever a história ou propor datas antes
 * de firmar (RN-25/RN-27, decisão de 2026-11-05: firmar primeiro), ou aceitar datas sem proposta nenhuma. */
public class IllegalPairingOperationException extends DuoHttpException {

	public IllegalPairingOperationException(String message) {
		super(HttpStatus.CONFLICT, message);
	}
}
