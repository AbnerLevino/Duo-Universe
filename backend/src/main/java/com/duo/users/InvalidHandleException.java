package com.duo.users;

import com.duo.shared.DuoHttpException;

import org.springframework.http.HttpStatus;

/** O @handle não tem o formato exigido (RN-30): 3 a 20 caracteres, letras, números ou "_". */
public class InvalidHandleException extends DuoHttpException {

	public InvalidHandleException() {
		super(HttpStatus.BAD_REQUEST, "Handle inválido: use 3 a 20 letras, números ou \"_\"");
	}
}
