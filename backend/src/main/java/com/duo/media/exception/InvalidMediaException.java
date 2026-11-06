package com.duo.media.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Arquivo recusado: vazio, grande demais, formato que não é JPEG/PNG/WebP de verdade, ou legenda longa. */
public class InvalidMediaException extends DuoHttpException {

	public InvalidMediaException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}
