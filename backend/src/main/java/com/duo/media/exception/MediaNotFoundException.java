package com.duo.media.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** A foto não existe ou não é para quem pediu — a mesma resposta nos dois casos, para não revelar que existe. */
public class MediaNotFoundException extends DuoHttpException {

	public MediaNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Foto não encontrada");
	}
}
