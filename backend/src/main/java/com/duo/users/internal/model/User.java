package com.duo.users.internal.model;

import java.time.Instant;
import java.time.LocalDate;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Column(name = "search_name", nullable = false, length = 100)
	private String searchName;

	@Column(length = 20)
	private String handle;

	@Column(length = 100)
	private String bio;

	@Column(length = 20)
	private String profession;

	@Enumerated(EnumType.STRING)
	@Column(name = "marital_status", nullable = false, length = 20)
	private MaritalStatus maritalStatus = MaritalStatus.SINGLE;

	@Enumerated(EnumType.STRING)
	@Column(name = "profile_visibility", nullable = false, length = 20)
	private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

	@Column(name = "handle_changes_on")
	private LocalDate handleChangesOn;

	@Column(name = "handle_changes_today", nullable = false)
	private int handleChangesToday;

	@Column(name = "avatar_media_id")
	private Long avatarMediaId;

	private Instant emailVerifiedAt;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant deletedAt;

	/** Moderação manual (Etapa 6): quem está oculto some da busca e das telas dos outros. */
	private Instant hiddenAt;

	protected User() {
	}

	public User(String googleSub, String email, String displayName) {
		this.googleSub = googleSub;
		this.email = email;
		this.displayName = displayName;
		this.searchName = NameNormalizer.normalize(displayName);
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
		this.searchName = NameNormalizer.normalize(displayName);
	}

	public String getSearchName() {
		return searchName;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getBio() {
		return bio;
	}

	public String getProfession() {
		return profession;
	}

	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}

	public ProfileVisibility getProfileVisibility() {
		return profileVisibility;
	}

	/** Troca tudo o que a pessoa edita no perfil de uma vez (o serviço já validou os valores). */
	public void updateProfile(String displayName, String bio, String profession, MaritalStatus maritalStatus,
			ProfileVisibility profileVisibility) {
		setDisplayName(displayName);
		this.bio = bio;
		this.profession = profession;
		this.maritalStatus = maritalStatus;
		this.profileVisibility = profileVisibility;
	}

	public LocalDate getHandleChangesOn() {
		return handleChangesOn;
	}

	public int getHandleChangesToday() {
		return handleChangesToday;
	}

	/** Quantas vezes o @handle foi trocado em {@code today}; muda de dia, zera. */
	public void recordHandleChange(LocalDate today) {
		if (!today.equals(handleChangesOn)) {
			handleChangesOn = today;
			handleChangesToday = 0;
		}
		handleChangesToday++;
	}

	public Long getAvatarMediaId() {
		return avatarMediaId;
	}

	public void setAvatarMediaId(Long avatarMediaId) {
		this.avatarMediaId = avatarMediaId;
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

	public Instant getHiddenAt() {
		return hiddenAt;
	}
}
