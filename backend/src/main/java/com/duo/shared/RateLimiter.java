package com.duo.shared;

import java.time.Duration;

import com.duo.shared.exception.RateLimitExceededException;

/**
 * Limite de tentativas por período (RA-08), guardado no banco — sobrevive a um reinício do servidor.
 * {@code scope} identifica a rota (ex.: {@code "pairing_invite_by_handle"}); {@code key} identifica quem
 * está tentando (hoje, o id da conta; o IP entra quando a rota tiver um controller HTTP).
 */
public interface RateLimiter {

	/**
	 * Registra uma tentativa de {@code key} em {@code scope} e verifica o limite.
	 *
	 * @throws RateLimitExceededException se {@code key} já tentou {@code maxAttempts} vezes dentro de
	 *         {@code window}
	 */
	void checkAndRecord(String scope, String key, int maxAttempts, Duration window);
}
