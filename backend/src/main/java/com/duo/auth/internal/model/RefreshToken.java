package com.duo.auth.internal.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Um token de renovação (RA-04). Só o hash é guardado: o token em si nunca vai para o banco (RA-09). */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
	private String tokenHash;

	@Column(name = "family_id", nullable = false, updatable = false)
	private UUID familyId;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "replaced_by")
	private Long replacedBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected RefreshToken() {
	}

	public RefreshToken(Long userId, String tokenHash, UUID familyId, Instant expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.familyId = familyId;
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public UUID getFamilyId() {
		return familyId;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public Long getReplacedBy() {
		return replacedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public boolean isExpired(Instant now) {
		return !expiresAt.isAfter(now);
	}

	/** Marca o token como usado/encerrado; {@code replacedBy} aponta o token novo quando foi uma rotação. */
	public void revoke(Instant when, Long replacedBy) {
		this.revokedAt = when;
		this.replacedBy = replacedBy;
	}
}
