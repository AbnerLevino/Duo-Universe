package com.duo.users.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** A busca precisa de pelo menos 3 letras (decisão do fundador, B4): termos curtos varreriam o Duo todo. */
public class InvalidSearchException extends DuoHttpException {

	public InvalidSearchException() {
		super(HttpStatus.BAD_REQUEST, "Digite pelo menos 3 letras para buscar");
	}
}
