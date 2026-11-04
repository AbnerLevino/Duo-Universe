package com.duo.relationships.internal;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * O Couple Universe (RN-06 a RN-08). Nesta etapa só o esqueleto do estado e das datas do casal; as
 * transições ({@code ACTIVE}→{@code PAUSED}→{@code ENDED}) e o nascimento a partir de um vínculo confirmado
 * chegam nas Etapas 8 e 10 (ver `docs/epico-2-vinculo.md`).
 */
@Entity
@Table(name = "couples")
public class Couple {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private State state;

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

	public Couple(State state) {
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public State getState() {
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

	/** RN-07: MVP com exatamente 2 membros por casal. */
	public enum State {
		ACTIVE, PAUSED, ENDED
	}
}
