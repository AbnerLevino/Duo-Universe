package com.duo.auth.dto;

import java.time.Instant;

/**
 * O token em texto puro só existe aqui, para ir ao cookie (RA-04); o banco guarda apenas o hash.
 * Não deve aparecer em log (RA-09), por isso o {@code toString} o esconde.
 */
public record IssuedRefreshToken(Long userId, String token, Instant expiresAt) {

	@Override
	public String toString() {
		return "IssuedRefreshToken[userId=" + userId + ", expiresAt=" + expiresAt + "]";
	}
}
