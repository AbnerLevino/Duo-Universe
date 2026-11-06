package com.duo.media.internal.model;

import java.time.Instant;

import com.duo.media.MediaKind;
import com.duo.media.MediaVisibility;
import com.duo.shared.Scope;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uma foto: de quem é, onde mora ({@code scope}), quem pode ver ({@code visibility}) e onde o arquivo está
 * guardado ({@code storageKey}). O acesso é decidido no backend por essas informações (RN-19, RN-20).
 */
@Entity
@Table(name = "media_assets")
public class MediaAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_user_id", nullable = false, updatable = false)
	private Long ownerUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 20)
	private MediaKind kind;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Scope scope;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MediaVisibility visibility;

	@Column(name = "storage_key", nullable = false, updatable = false, length = 200)
	private String storageKey;

	@Column(name = "content_type", nullable = false, updatable = false, length = 50)
	private String contentType;

	@Column(name = "size_bytes", nullable = false, updatable = false)
	private long sizeBytes;

	/** Hash hexadecimal de tamanho fixo (64): por isso a coluna é CHAR, não VARCHAR. */
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 64)
	private String sha256;

	@Column(length = 200)
	private String caption;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "hidden_at")
	private Instant hiddenAt;

	protected MediaAsset() {
	}

	public MediaAsset(Long ownerUserId, MediaKind kind, Scope scope, MediaVisibility visibility, String storageKey,
			String contentType, long sizeBytes, String sha256, String caption) {
		this.ownerUserId = ownerUserId;
		this.kind = kind;
		this.scope = scope;
		this.visibility = visibility;
		this.storageKey = storageKey;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.sha256 = sha256;
		this.caption = caption;
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public MediaKind getKind() {
		return kind;
	}

	public Scope getScope() {
		return scope;
	}

	public MediaVisibility getVisibility() {
		return visibility;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public String getSha256() {
		return sha256;
	}

	public String getCaption() {
		return caption;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getHiddenAt() {
		return hiddenAt;
	}

	public void edit(String caption, MediaVisibility visibility) {
		this.caption = caption;
		this.visibility = visibility;
	}
}
