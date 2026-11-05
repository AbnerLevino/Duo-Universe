package com.duo.users;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Exposição HTTP da escolha do @handle (Etapa 4/13) — a tela obrigatória do próximo login (Etapa 14) usa isto. */
@RestController
class UserController {

	private final UserService users;

	UserController(UserService users) {
		this.users = users;
	}

	record ChooseHandleRequest(String handle) {
	}

	@PutMapping("/api/me/handle")
	UserSummary chooseHandle(@AuthenticationPrincipal Jwt jwt, @RequestBody ChooseHandleRequest request) {
		return users.chooseHandle(Long.valueOf(jwt.getSubject()), request.handle());
	}
}
