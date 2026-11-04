package com.duo.relationships;

import java.time.Instant;

/** O que outros módulos (e, mais adiante, os controllers) podem saber sobre um convite, sem acessar a
 * entidade JPA direto. */
public record PairingSummary(
		Long id,
		Long initiatorUserId,
		Long inviteeUserId,
		Channel channel,
		PairingState state,
		Instant expiresAt,
		Instant createdAt) {
}
