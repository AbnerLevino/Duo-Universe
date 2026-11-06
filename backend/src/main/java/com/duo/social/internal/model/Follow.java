package com.duo.social.internal.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** {@code followerId} é Duollower de {@code followeeId}. O banco garante um par único e ninguém seguir a si mesmo. */
@Entity
@Table(name = "follows")
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "follower_id", nullable = false, updatable = false)
	private Long followerId;

	@Column(name = "followee_id", nullable = false, updatable = false)
	private Long followeeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FollowStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "responded_at")
	private Instant respondedAt;

	protected Follow() {
	}

	public Follow(Long followerId, Long followeeId, FollowStatus status) {
		this.followerId = followerId;
		this.followeeId = followeeId;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public Long getFollowerId() {
		return followerId;
	}

	public Long getFolloweeId() {
		return followeeId;
	}

	public FollowStatus getStatus() {
		return status;
	}

	/** Só um pedido pendente pode ser aceito; aceitar de novo não muda nada. */
	public void accept(Instant when) {
		if (status == FollowStatus.PENDING) {
			status = FollowStatus.ACCEPTED;
			respondedAt = when;
		}
	}
}
