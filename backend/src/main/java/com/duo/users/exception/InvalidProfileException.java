package com.duo.users.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Algum campo do perfil fora das regras (nome de 2 a 50, bio até 100, profissão até 20). */
public class InvalidProfileException extends DuoHttpException {

	public InvalidProfileException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}
