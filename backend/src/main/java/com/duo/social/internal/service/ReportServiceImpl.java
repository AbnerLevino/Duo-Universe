package com.duo.social.internal.service;

import java.time.Duration;

import com.duo.shared.RateLimiter;
import com.duo.social.ReportReason;
import com.duo.social.ReportService;
import com.duo.social.exception.InvalidSocialActionException;
import com.duo.social.internal.model.Report;
import com.duo.social.internal.model.ReportTarget;
import com.duo.social.internal.repository.ReportRepository;
import com.duo.users.dto.UserProfile;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
class ReportServiceImpl implements ReportService {

	private static final String RATE_SCOPE = "report";
	private static final int MAX_REPORTS_PER_HOUR = 20;
	private static final int MAX_DETAILS = 300;

	private final ReportRepository reports;
	private final ProfileLookup lookup;
	private final RateLimiter rateLimiter;

	ReportServiceImpl(ReportRepository reports, ProfileLookup lookup, RateLimiter rateLimiter) {
		this.reports = reports;
		this.lookup = lookup;
		this.rateLimiter = rateLimiter;
	}

	@Override
	public void reportProfile(Long reporterId, String handle, ReportReason reason, String details) {
		UserProfile target = lookup.requireVisible(reporterId, handle);
		save(reporterId, ReportTarget.PROFILE, target.id(), null, reason, details);
	}

	@Override
	public void reportMedia(Long reporterId, Long ownerId, Long mediaId, ReportReason reason, String details) {
		save(reporterId, ReportTarget.PHOTO, ownerId, mediaId, reason, details);
	}

	/**
	 * Sem transação própria: uma denúncia repetida bate no índice único e é aceita em silêncio (a pessoa
	 * não precisa saber que já tinha denunciado).
	 */
	private void save(Long reporterId, ReportTarget type, Long targetUserId, Long mediaId, ReportReason reason,
			String details) {
		if (reporterId.equals(targetUserId)) {
			throw new InvalidSocialActionException("Você não pode denunciar a si mesmo");
		}
		if (reason == null) {
			throw new InvalidSocialActionException("Escolha o motivo da denúncia");
		}
		String text = details == null || details.isBlank() ? null : details.trim();
		if (text != null && text.codePointCount(0, text.length()) > MAX_DETAILS) {
			throw new InvalidSocialActionException("O texto da denúncia pode ter até 300 caracteres");
		}
		if (reason == ReportReason.OTHER && text == null) {
			throw new InvalidSocialActionException("Explique o motivo da denúncia");
		}
		rateLimiter.checkAndRecord(RATE_SCOPE, reporterId.toString(), MAX_REPORTS_PER_HOUR, Duration.ofHours(1));
		try {
			reports.saveAndFlush(new Report(reporterId, type, targetUserId, mediaId, reason, text));
		}
		catch (DataIntegrityViolationException alreadyReported) {
			// mesma denúncia do mesmo denunciante: nada a fazer
		}
	}
}
