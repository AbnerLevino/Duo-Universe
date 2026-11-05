package com.duo.relationships;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Mensagem genérica de propósito (RA-07/RN-30, DEC-031): não revela se o @handle simplesmente não existe,
 * se é da própria pessoa que está convidando, ou se já pertence a alguém em vínculo ACTIVE/PAUSED. */
public class HandleNotInvitableException extends DuoHttpException {

	public HandleNotInvitableException() {
		super(HttpStatus.CONFLICT, "Esse @handle não pode ser convidado agora.");
	}
}
