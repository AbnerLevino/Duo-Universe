package com.duo.users.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** O @handle já pertence a outra conta (RN-30). Comparação sem diferenciar maiúsculas, como o e-mail. */
public class HandleTakenException extends DuoHttpException {

	public HandleTakenException() {
		super(HttpStatus.CONFLICT, "Handle já está em uso");
	}
}
