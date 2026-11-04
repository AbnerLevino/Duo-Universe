package com.duo.auth;

import java.time.Instant;

/** Token de acesso (JWT) de vida curta. Não deve aparecer em log (RA-09), por isso o {@code toString} o esconde. */
public record AccessToken(String value, Instant expiresAt) {

	@Override
	public String toString() {
		return "AccessToken[expiresAt=" + expiresAt + "]";
	}
}
