package com.duo.auth;

import com.duo.users.UserService;
import com.duo.users.UserSummary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quem sou eu? Serve para provar o login. A identidade vem do usuário autenticado,
 * nunca de um id enviado pelo frontend (regra 11).
 */
@RestController
class MeController {

	private final UserService userService;

	MeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/api/me")
	ResponseEntity<UserSummary> me(@AuthenticationPrincipal OidcUser google) {
		return userService.findByGoogleSub(google.getSubject())
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.status(401).build());
	}
}
