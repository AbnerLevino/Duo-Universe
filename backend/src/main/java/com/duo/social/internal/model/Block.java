package com.duo.social.internal.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** {@code blockerId} bloqueou {@code blockedId}: um some para o outro (busca, perfil, fotos). */
@Entity
@Table(name = "blocks")
public class Block {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "blocker_id", nullable = false, updatable = false)
	private Long blockerId;

	@Column(name = "blocked_id", nullable = false, updatable = false)
	private Long blockedId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Block() {
	}

	public Block(Long blockerId, Long blockedId) {
		this.blockerId = blockerId;
		this.blockedId = blockedId;
	}

	public Long getBlockerId() {
		return blockerId;
	}

	public Long getBlockedId() {
		return blockedId;
	}
}
