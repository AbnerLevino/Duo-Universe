package com.duo.auth;

import com.duo.users.UserService;
import com.duo.users.UserSummary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quem sou eu? A identidade vem do token de acesso já validado pelo Spring Security,
 * nunca de um id enviado pelo frontend (regra 11).
 */
@RestController
class MeController {

	private final UserService userService;

	MeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/api/me")
	ResponseEntity<UserSummary> me(@AuthenticationPrincipal Jwt jwt) {
		return userService.findById(Long.valueOf(jwt.getSubject()))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.status(401).build());
	}
}
