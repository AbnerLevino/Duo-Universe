package com.duo.users;

/**
 * O e-mail já pertence a outra conta (outro {@code googleSub}). Não se junta contas sozinho:
 * quem controla o e-mail no Google não é, necessariamente, o dono da conta antiga.
 */
public class EmailAlreadyLinkedException extends RuntimeException {

	public EmailAlreadyLinkedException() {
		super("E-mail já vinculado a outra conta");
	}
}
