package com.duo.users.internal;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "google_sub", nullable = false, unique = true)
	private String googleSub;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(length = 20)
	private String handle;

	private Instant emailVerifiedAt;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant deletedAt;

	protected User() {
	}

	public User(String googleSub, String email, String displayName) {
		this.googleSub = googleSub;
		this.email = email;
		this.displayName = displayName;
	}

	public Long getId() {
		return id;
	}

	public String getGoogleSub() {
		return googleSub;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public Instant getEmailVerifiedAt() {
		return emailVerifiedAt;
	}

	public void markEmailVerified(Instant when) {
		this.emailVerifiedAt = when;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void softDelete(Instant when) {
		this.deletedAt = when;
	}
}
