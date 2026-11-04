package com.duo.relationships.internal;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.duo.relationships.Channel;
import com.duo.relationships.IllegalPairingTransitionException;
import com.duo.relationships.PairingState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * O convite para o vínculo (DEC-012). Etapa 2: só a máquina de estados pura — quem convida de verdade por
 * @handle ou por link (Etapas 3 e 5) e quem confirma os dois lados (Etapa 8) ainda não existem.
 */
@Entity
@Table(name = "couple_pairings")
public class CouplePairing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "initiator_user_id", nullable = false, updatable = false)
	private Long initiatorUserId;

	@Column(name = "invitee_user_id")
	private Long inviteeUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10, updatable = false)
	private Channel channel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private PairingState state;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "decided_at")
	private Instant decidedAt;

	@Column(name = "couple_id")
	private Long coupleId;

	protected CouplePairing() {
	}

	public CouplePairing(Long initiatorUserId, Long inviteeUserId, Channel channel, Instant expiresAt) {
		this.initiatorUserId = initiatorUserId;
		this.inviteeUserId = inviteeUserId;
		this.channel = channel;
		this.state = PairingState.PENDING;
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public Long getInitiatorUserId() {
		return initiatorUserId;
	}

	public Long getInviteeUserId() {
		return inviteeUserId;
	}

	public Channel getChannel() {
		return channel;
	}

	public PairingState getState() {
		return state;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getDecidedAt() {
		return decidedAt;
	}

	public Long getCoupleId() {
		return coupleId;
	}

	/** Só conta como vencido enquanto ainda não foi decidido — um pareamento já CANCELLED não "expira" de novo. */
	public boolean isExpired(Instant now) {
		return state == PairingState.PENDING && !expiresAt.isAfter(now);
	}

	public boolean involves(Long userId) {
		return initiatorUserId.equals(userId) || (inviteeUserId != null && inviteeUserId.equals(userId));
	}

	/** RN-29: recusa, cancelamento e expiração levam ao mesmo estado final (ver a matriz em docs/epico-2-vinculo.md). */
	public void cancel(Instant when) {
		requireTransitionFrom(PairingState.CANCELLED);
		this.state = PairingState.CANCELLED;
		this.decidedAt = when;
	}

	/** Usado pela Etapa 8, quando os dois lados já confirmaram e o Couple nasceu. */
	public void markConfirmed(Instant when, Long coupleId) {
		requireTransitionFrom(PairingState.CONFIRMED);
		this.state = PairingState.CONFIRMED;
		this.decidedAt = when;
		this.coupleId = coupleId;
	}

	private void requireTransitionFrom(PairingState target) {
		if (state != PairingState.PENDING) {
			throw new IllegalPairingTransitionException(state, target);
		}
	}
}
