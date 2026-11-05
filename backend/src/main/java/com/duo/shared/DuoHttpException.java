package com.duo.shared;

import org.springframework.http.HttpStatus;

/**
 * Base para as exceções de negócio dos módulos que viram resposta HTTP direto, sem o
 * {@link GlobalExceptionHandler} precisar conhecer cada tipo de exceção de cada módulo — isso criaria um
 * ciclo (shared dependeria de volta dos módulos que já dependem de shared). Cada módulo decide o status
 * certo para a própria exceção; o `shared` só sabe aplicá-lo.
 */
public abstract class DuoHttpException extends RuntimeException {

	private final HttpStatus httpStatus;

	protected DuoHttpException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public HttpStatus httpStatus() {
		return httpStatus;
	}
}
