package com.duo.media.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** A pessoa já guardou o máximo de fotos ou de espaço do limite provisório (RN-22; Q-20 completa depois). */
public class MediaQuotaExceededException extends DuoHttpException {

	public MediaQuotaExceededException() {
		super(HttpStatus.CONFLICT, "Você atingiu o limite de fotos. Apague alguma para enviar outra.");
	}
}
