package com.duo.social.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Não há um pedido de seguir pendente dessa pessoa. */
public class FollowRequestNotFoundException extends DuoHttpException {

	public FollowRequestNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Pedido não encontrado");
	}
}
