package com.duo.relationships.dto;

import java.time.Instant;

/**
 * O código do convite em texto puro só existe aqui, para A mandar por fora (WhatsApp etc.); o banco guarda
 * apenas o hash (RA-09). Não deve aparecer em log, por isso o {@code toString} o esconde.
 */
public record IssuedPairingInvite(Long pairingId, String code, Instant expiresAt) {

	@Override
	public String toString() {
		return "IssuedPairingInvite[pairingId=" + pairingId + ", expiresAt=" + expiresAt + "]";
	}
}
