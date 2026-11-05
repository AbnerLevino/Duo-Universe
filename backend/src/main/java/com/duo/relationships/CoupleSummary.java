package com.duo.relationships;

import java.time.Instant;
import java.time.LocalDate;

/** O que outros módulos (e, mais adiante, os controllers) podem saber sobre um casal, sem acessar a
 * entidade JPA direto. */
public record CoupleSummary(
		Long id,
		CoupleState state,
		LocalDate knownSinceDate,
		LocalDate datingSinceDate,
		Instant pausedAt,
		Instant endedAt) {
}
