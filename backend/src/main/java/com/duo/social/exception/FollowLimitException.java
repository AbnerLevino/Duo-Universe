package com.duo.social.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Pedidos pendentes demais ao mesmo tempo (antispam). */
public class FollowLimitException extends DuoHttpException {

	public FollowLimitException() {
		super(HttpStatus.CONFLICT, "Você já tem pedidos pendentes demais. Aguarde alguns serem respondidos.");
	}
}
