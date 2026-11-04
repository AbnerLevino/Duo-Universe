package com.duo.relationships;

/** Mensagem genérica de propósito: código inexistente, vencido, já usado ou a própria pessoa que criou o
 * link tentando usá-lo caem todos aqui — não revelar qual dos quatro motivos é o real. */
public class InviteCodeInvalidException extends RuntimeException {

	public InviteCodeInvalidException() {
		super("Código de convite inválido ou vencido.");
	}
}
