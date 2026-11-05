package com.duo.relationships.internal;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.duo.relationships.Channel;
import com.duo.relationships.IllegalPairingTransitionException;
import com.duo.relationships.InviteCodeInvalidException;
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

	@Column(name = "invite_code_hash", length = 64, updatable = false)
	private String inviteCodeHash;

	@Column(name = "date_proposed_by_user_id")
	private Long dateProposedByUserId;

	@Column(name = "proposed_known_since_date")
	private LocalDate proposedKnownSinceDate;

	@Column(name = "proposed_dating_since_date")
	private LocalDate proposedDatingSinceDate;

	@Column(name = "dates_accepted_at")
	private Instant datesAcceptedAt;

	protected CouplePairing() {
	}

	public CouplePairing(Long initiatorUserId, Long inviteeUserId, Channel channel, Instant expiresAt) {
		this(initiatorUserId, inviteeUserId, channel, expiresAt, null);
	}

	/** Usado pelo convite por link (Etapa 3): {@code inviteeUserId} começa nulo até alguém "redimir" o código. */
	public CouplePairing(
			Long initiatorUserId, Long inviteeUserId, Channel channel, Instant expiresAt, String inviteCodeHash) {
		this.initiatorUserId = initiatorUserId;
		this.inviteeUserId = inviteeUserId;
		this.channel = channel;
		this.state = PairingState.PENDING;
		this.expiresAt = expiresAt;
		this.inviteCodeHash = inviteCodeHash;
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

	public String getInviteCodeHash() {
		return inviteCodeHash;
	}

	/** Só conta como vencido enquanto ainda não foi decidido — um pareamento já CANCELLED não "expira" de novo. */
	public boolean isExpired(Instant now) {
		return state == PairingState.PENDING && !expiresAt.isAfter(now);
	}

	public boolean involves(Long userId) {
		return initiatorUserId.equals(userId) || (inviteeUserId != null && inviteeUserId.equals(userId));
	}

	/**
	 * A outra pessoa do pareamento. Devolve {@code null} quando é um convite por link que ainda não foi
	 * usado (sem convidado definido ainda) — não existe "parceiro" para ler a história nesse caso.
	 */
	public Long partnerOf(Long userId) {
		if (initiatorUserId.equals(userId)) {
			return inviteeUserId;
		}
		if (inviteeUserId != null && inviteeUserId.equals(userId)) {
			return initiatorUserId;
		}
		throw new IllegalArgumentException("userId não participa deste pareamento");
	}

	/** RN-29: recusa, cancelamento e expiração levam ao mesmo estado final (ver a matriz em docs/epico-2-vinculo.md). */
	public void cancel(Instant when) {
		requireTransitionFrom(PairingState.CANCELLED);
		this.state = PairingState.CANCELLED;
		this.decidedAt = when;
	}

	/**
	 * Preenche o convidado quando alguém usa o código do link (Etapa 3). Uso único: falha se o pareamento já
	 * não estiver mais {@code PENDING} (cancelado, vencido ou já redimido por outra pessoa) ou se a própria
	 * pessoa que criou o link tentar usá-lo.
	 */
	public void redeemLink(Long inviteeUserId, Instant now) {
		if (state != PairingState.PENDING || isExpired(now) || this.inviteeUserId != null
				|| initiatorUserId.equals(inviteeUserId)) {
			throw new InviteCodeInvalidException();
		}
		this.inviteeUserId = inviteeUserId;
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

	/**
	 * P3/RN-27: propõe as duas datas do casal. Qualquer proposta nova — inclusive uma contraproposta da outra
	 * pessoa — reabre o consenso: uma aceitação anterior é descartada.
	 */
	public void proposeDates(Long proposedByUserId, LocalDate knownSinceDate, LocalDate datingSinceDate) {
		requireStillPending();
		this.dateProposedByUserId = proposedByUserId;
		this.proposedKnownSinceDate = knownSinceDate;
		this.proposedDatingSinceDate = datingSinceDate;
		this.datesAcceptedAt = null;
	}

	/** Só a outra pessoa pode aceitar — nunca quem propôs — e só existe algo para aceitar depois de uma proposta. */
	public void acceptDates(Long acceptingUserId, Instant when) {
		requireStillPending();
		if (dateProposedByUserId == null) {
			throw new IllegalStateException("Ninguém propôs datas ainda.");
		}
		if (dateProposedByUserId.equals(acceptingUserId)) {
			throw new IllegalStateException("Quem propôs as datas não pode aceitar a própria proposta.");
		}
		this.datesAcceptedAt = when;
	}

	public boolean areDatesAccepted() {
		return datesAcceptedAt != null;
	}

	public Long getDateProposedByUserId() {
		return dateProposedByUserId;
	}

	public LocalDate getProposedKnownSinceDate() {
		return proposedKnownSinceDate;
	}

	public LocalDate getProposedDatingSinceDate() {
		return proposedDatingSinceDate;
	}

	public Instant getDatesAcceptedAt() {
		return datesAcceptedAt;
	}

	private void requireStillPending() {
		if (state != PairingState.PENDING) {
			throw new IllegalStateException("O pareamento já foi decidido; não dá para propor ou aceitar datas.");
		}
	}
}
