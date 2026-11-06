package com.duo.users.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** O DuoName só pode ser trocado 2 vezes por dia (decisão do fundador, 2026-11-06). */
public class HandleChangeLimitException extends DuoHttpException {

	public HandleChangeLimitException() {
		super(HttpStatus.TOO_MANY_REQUESTS, "Você já trocou o DuoName 2 vezes hoje. Tente amanhã.");
	}
}
