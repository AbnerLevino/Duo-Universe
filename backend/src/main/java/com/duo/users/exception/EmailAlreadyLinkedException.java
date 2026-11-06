package com.duo.users.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/**
 * O e-mail já pertence a outra conta (outro {@code googleSub}). Não se junta contas sozinho:
 * quem controla o e-mail no Google não é, necessariamente, o dono da conta antiga.
 */
public class EmailAlreadyLinkedException extends DuoHttpException {

	public EmailAlreadyLinkedException() {
		super(HttpStatus.CONFLICT, "E-mail já vinculado a outra conta");
	}
}
