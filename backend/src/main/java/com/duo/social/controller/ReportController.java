package com.duo.social.controller;

import com.duo.social.ReportReason;
import com.duo.social.ReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Denunciar um perfil (DEC-035). Fica fora do {@code ProfileController} porque este continua ligado mesmo com a
 * camada social congelada (RF-A11): denúncia e bloqueio protegem o convite de vínculo, não são "rede social".
 */
@RestController
class ReportController {

	private final ReportService reports;

	ReportController(ReportService reports) {
		this.reports = reports;
	}

	record ReportRequest(ReportReason reason, String details) {
	}

	@PostMapping("/api/profiles/{handle}/report")
	ResponseEntity<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle,
			@RequestBody ReportRequest request) {
		reports.reportProfile(Long.valueOf(jwt.getSubject()), handle, request.reason(), request.details());
		return ResponseEntity.noContent().build();
	}
}
