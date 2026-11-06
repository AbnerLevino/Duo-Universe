package com.duo.social.internal.model;

import java.time.Instant;

import com.duo.social.ReportReason;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Uma denúncia, guardada para revisão manual. Quem denunciou nunca é revelado à pessoa denunciada. */
@Entity
@Table(name = "reports")
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reporter_id", nullable = false, updatable = false)
	private Long reporterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, updatable = false, length = 20)
	private ReportTarget targetType;

	@Column(name = "target_user_id", nullable = false, updatable = false)
	private Long targetUserId;

	@Column(name = "target_media_id", updatable = false)
	private Long targetMediaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = 30)
	private ReportReason reason;

	@Column(updatable = false, length = 300)
	private String details;

	@Column(nullable = false, length = 20)
	private String status = "NEW";

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Report() {
	}

	public Report(Long reporterId, ReportTarget targetType, Long targetUserId, Long targetMediaId,
			ReportReason reason, String details) {
		this.reporterId = reporterId;
		this.targetType = targetType;
		this.targetUserId = targetUserId;
		this.targetMediaId = targetMediaId;
		this.reason = reason;
		this.details = details;
	}

	public Long getId() {
		return id;
	}
}
