package com.duo.social.controller;

import com.duo.social.ProfileViewService;
import com.duo.social.ReportReason;
import com.duo.social.ReportService;
import com.duo.social.dto.PublicProfile;
import com.duo.social.dto.SearchPage;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Ver o perfil de alguém, buscar pessoas e denunciar um perfil. Quem pergunta é sempre a pessoa do token. */
@RestController
class ProfileController {

	private final ProfileViewService profiles;
	private final ReportService reports;

	ProfileController(ProfileViewService profiles, ReportService reports) {
		this.profiles = profiles;
		this.reports = reports;
	}

	record ReportRequest(ReportReason reason, String details) {
	}

	@GetMapping("/api/profiles/{handle}")
	PublicProfile view(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		return profiles.view(userId(jwt), handle);
	}

	@GetMapping("/api/search/profiles")
	SearchPage search(@AuthenticationPrincipal Jwt jwt, @RequestParam("q") String query,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		return profiles.search(userId(jwt), query, page);
	}

	@PostMapping("/api/profiles/{handle}/report")
	ResponseEntity<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle,
			@RequestBody ReportRequest request) {
		reports.reportProfile(userId(jwt), handle, request.reason(), request.details());
		return ResponseEntity.noContent().build();
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
