package com.duo.social.controller;

import com.duo.social.ProfileViewService;
import com.duo.social.SocialFeature;
import com.duo.social.dto.PublicProfile;
import com.duo.social.dto.SearchPage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ver o perfil de alguém e buscar pessoas. Quem pergunta é sempre a pessoa do token. Congelado (RF-A11): só
 * existe com {@code duo.features.social.enabled=true}; a denúncia mora no {@code ReportController}.
 */
@RestController
@ConditionalOnProperty(name = SocialFeature.PROPERTY, havingValue = "true")
class ProfileController {

	private final ProfileViewService profiles;

	ProfileController(ProfileViewService profiles) {
		this.profiles = profiles;
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

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
