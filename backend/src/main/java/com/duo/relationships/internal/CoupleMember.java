package com.duo.relationships.internal;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uma pessoa dentro de um {@link Couple}. RN-06: enquanto {@code leftAt} for nulo, a pessoa está "dentro"
 * do vínculo ({@code ACTIVE} ou {@code PAUSED}) — o índice único parcial da migration garante que isso só
 * vale para um vínculo por vez.
 */
@Entity
@Table(name = "couple_members")
public class CoupleMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "couple_id", nullable = false, updatable = false)
	private Long coupleId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private Instant joinedAt;

	@Column(name = "left_at")
	private Instant leftAt;

	protected CoupleMember() {
	}

	public CoupleMember(Long coupleId, Long userId) {
		this.coupleId = coupleId;
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public Long getCoupleId() {
		return coupleId;
	}

	public Long getUserId() {
		return userId;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public Instant getLeftAt() {
		return leftAt;
	}

	public boolean isActive() {
		return leftAt == null;
	}

	/** RN-14/15: sair é individual; nada além desta marca é apagado. */
	public void leave(Instant when) {
		this.leftAt = when;
	}
}
