package com.duo.users.controller;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;
import com.duo.users.UserService;
import com.duo.users.dto.ProfileUpdate;
import com.duo.users.dto.UserProfile;
import com.duo.users.dto.UserSummary;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposição HTTP da conta: escolher o DuoName (Etapa 4/13, a tela obrigatória do próximo login) e ver/editar o
 * próprio perfil (Épico 3). A pessoa é sempre a do token (regra 11), nunca um id vindo do frontend.
 */
@RestController
class UserController {

	private final UserService users;

	UserController(UserService users) {
		this.users = users;
	}

	record ChooseHandleRequest(String handle) {
	}

	record UpdateProfileRequest(String displayName, String bio, String profession, MaritalStatus maritalStatus,
			ProfileVisibility profileVisibility) {
	}

	@GetMapping("/api/me/profile")
	UserProfile myProfile(@AuthenticationPrincipal Jwt jwt) {
		return users.findProfileById(Long.valueOf(jwt.getSubject())).orElseThrow();
	}

	@PutMapping("/api/me/profile")
	UserProfile updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateProfileRequest request) {
		return users.updateProfile(Long.valueOf(jwt.getSubject()), new ProfileUpdate(request.displayName(),
				request.bio(), request.profession(), request.maritalStatus(), request.profileVisibility()));
	}

	@PutMapping("/api/me/handle")
	UserSummary chooseHandle(@AuthenticationPrincipal Jwt jwt, @RequestBody ChooseHandleRequest request) {
		return users.chooseHandle(Long.valueOf(jwt.getSubject()), request.handle());
	}
}
