package com.duo.shared.exception;

import org.springframework.http.HttpStatus;

/** RA-08: {@code key} tentou mais vezes do que o permitido dentro da janela de tempo. */
public class RateLimitExceededException extends DuoHttpException {

	public RateLimitExceededException() {
		super(HttpStatus.TOO_MANY_REQUESTS, "Limite de tentativas excedido. Tente novamente mais tarde.");
	}
}
