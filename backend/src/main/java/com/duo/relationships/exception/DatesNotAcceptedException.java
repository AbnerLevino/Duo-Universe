package com.duo.relationships.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** RN-27: as duas datas do casal (quando se conheceram, quando começaram a namorar) ainda não foram aceitas
 * pelos dois — falta alguém propor, ou falta a outra pessoa aceitar. */
public class DatesNotAcceptedException extends DuoHttpException {

	public DatesNotAcceptedException() {
		super(HttpStatus.CONFLICT, "As datas do casal ainda não foram aceitas pelos dois.");
	}
}
