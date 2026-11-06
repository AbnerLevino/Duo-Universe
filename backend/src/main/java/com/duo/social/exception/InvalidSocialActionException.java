package com.duo.social.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Ação que não faz sentido (seguir, bloquear ou denunciar a si mesmo; denúncia sem o texto exigido). */
public class InvalidSocialActionException extends DuoHttpException {

	public InvalidSocialActionException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}
