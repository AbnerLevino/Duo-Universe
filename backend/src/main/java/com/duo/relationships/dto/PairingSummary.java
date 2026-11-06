package com.duo.relationships.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.duo.relationships.Channel;
import com.duo.relationships.PairingState;

/** O que outros módulos (e os controllers) podem saber sobre um convite, sem acessar a entidade JPA direto. */
public record PairingSummary(
		Long id,
		Long initiatorUserId,
		Long inviteeUserId,
		Channel channel,
		PairingState state,
		Instant expiresAt,
		Instant createdAt,
		Long coupleId,
		Instant initiatorConfirmedAt,
		Instant inviteeConfirmedAt,
		Long dateProposedByUserId,
		LocalDate proposedKnownSinceDate,
		LocalDate proposedDatingSinceDate,
		Instant datesAcceptedAt) {
}
