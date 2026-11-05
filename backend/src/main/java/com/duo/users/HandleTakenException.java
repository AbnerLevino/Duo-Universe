package com.duo.users;

/** O @handle já pertence a outra conta (RN-30). Comparação sem diferenciar maiúsculas, como o e-mail. */
public class HandleTakenException extends RuntimeException {

	public HandleTakenException() {
		super("Handle já está em uso");
	}
}
