package com.duo.relationships;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** Mensagem genérica de propósito: código inexistente, vencido, já usado ou a própria pessoa que criou o
 * link tentando usá-lo caem todos aqui — não revelar qual dos quatro motivos é o real. */
public class InviteCodeInvalidException extends DuoHttpException {

	public InviteCodeInvalidException() {
		super(HttpStatus.NOT_FOUND, "Código de convite inválido ou vencido.");
	}
}
