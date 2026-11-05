package com.duo.relationships;

/** A história completa de uma pessoa, devolvida só quando as duas do pareamento já terminaram (RN-26). */
public record StorySummary(
		Long pairingId,
		Long userId,
		String howWeMet,
		String timeBeforeDating,
		String whatIThoughtThen,
		String whatIThinkNow) {
}
