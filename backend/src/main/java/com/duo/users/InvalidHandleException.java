package com.duo.users;

/** O @handle não tem o formato exigido (RN-30): 3 a 20 caracteres, letras, números ou "_". */
public class InvalidHandleException extends RuntimeException {

	public InvalidHandleException() {
		super("Handle inválido: use 3 a 20 letras, números ou \"_\"");
	}
}
