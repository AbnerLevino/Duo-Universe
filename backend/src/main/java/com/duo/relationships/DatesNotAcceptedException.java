package com.duo.relationships;

/** RN-27: as duas datas do casal (quando se conheceram, quando começaram a namorar) ainda não foram aceitas
 * pelos dois — falta alguém propor, ou falta a outra pessoa aceitar. */
public class DatesNotAcceptedException extends RuntimeException {

	public DatesNotAcceptedException() {
		super("As datas do casal ainda não foram aceitas pelos dois.");
	}
}
