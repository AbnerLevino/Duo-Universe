package com.duo.relationships.internal;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.duo.relationships.CoupleState;
import com.duo.relationships.IllegalCoupleTransitionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * O Couple Universe (RN-06 a RN-08, RN-13 a RN-18). O nascimento a partir de um vínculo confirmado chega na
 * Etapa 8 (ver `docs/epico-2-vinculo.md`); pausar/retomar/sair (Etapa 10) já estão aqui.
 */
@Entity
@Table(name = "couples")
public class Couple {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private CoupleState state;

	@Column(name = "known_since_date")
	private LocalDate knownSinceDate;

	@Column(name = "dating_since_date")
	private LocalDate datingSinceDate;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "paused_at")
	private Instant pausedAt;

	@Column(name = "ended_at")
	private Instant endedAt;

	protected Couple() {
	}

	public Couple(CoupleState state) {
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public CoupleState getState() {
		return state;
	}

	public LocalDate getKnownSinceDate() {
		return knownSinceDate;
	}

	public LocalDate getDatingSinceDate() {
		return datingSinceDate;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getPausedAt() {
		return pausedAt;
	}

	public Instant getEndedAt() {
		return endedAt;
	}

	/**
	 * RN-28: o Couple nasce sem datas (Etapa 8) — elas só existem depois que o pareamento de origem tem as
	 * duas aceitas (Etapa 7, já com o vínculo firmado). Chamado por {@code CouplePairingServiceImpl.acceptDates}.
	 */
	public void recordDates(LocalDate knownSinceDate, LocalDate datingSinceDate) {
		this.knownSinceDate = knownSinceDate;
		this.datingSinceDate = datingSinceDate;
	}

	/** RN-13: qualquer membro pausa sozinho; nada é apagado — só o estado muda. */
	public void pause(Instant when) {
		requireCurrentState(CoupleState.ACTIVE, CoupleState.PAUSED);
		this.state = CoupleState.PAUSED;
		this.pausedAt = when;
	}

	/** Retomar só funciona a partir de PAUSED (matriz em `docs/epico-2-vinculo.md`). */
	public void resume(Instant when) {
		requireCurrentState(CoupleState.PAUSED, CoupleState.ACTIVE);
		this.state = CoupleState.ACTIVE;
		this.pausedAt = null;
	}

	/** RN-14/17/18: termina o vínculo (de ACTIVE ou PAUSED); o centro do casal em si nunca é apagado, só o
	 * estado muda para ENDED (final — RN-17a cuida da visão de cada ex-membro, não desta classe). */
	public void end(Instant when) {
		if (state == CoupleState.ENDED) {
			throw new IllegalCoupleTransitionException(state, CoupleState.ENDED);
		}
		this.state = CoupleState.ENDED;
		this.endedAt = when;
	}

	private void requireCurrentState(CoupleState expected, CoupleState attemptedTarget) {
		if (state != expected) {
			throw new IllegalCoupleTransitionException(state, attemptedTarget);
		}
	}
}
