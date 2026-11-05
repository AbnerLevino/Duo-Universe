package com.duo.shared;

/** RA-08: {@code key} tentou mais vezes do que o permitido dentro da janela de tempo. */
public class RateLimitExceededException extends RuntimeException {

	public RateLimitExceededException() {
		super("Limite de tentativas excedido. Tente novamente mais tarde.");
	}
}
