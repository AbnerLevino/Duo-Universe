package com.duo.auth;

import java.io.IOException;

import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Atalho só para testar localmente, sem passar pelo Google: abre uma sessão de verdade (RA-04/RA-05) para
 * uma conta fictícia, reaproveitando o mesmo caminho do login real ({@link UserService#findOrCreateByGoogle}).
 * Só existe quando o backend roda com o perfil {@code dev} (ex.: {@code SPRING_PROFILES_ACTIVE=dev}); fora
 * desse perfil este bean nem é criado e a rota não existe — nunca ativar esse perfil num ambiente real.
 */
@RestController
@Profile("dev")
class DevLoginController {

	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final RefreshCookies cookies;
	private final String frontendUrl;

	DevLoginController(UserService userService, RefreshTokenService refreshTokenService, RefreshCookies cookies,
			@Value("${duo.frontend-url}") String frontendUrl) {
		this.userService = userService;
		this.refreshTokenService = refreshTokenService;
		this.cookies = cookies;
		this.frontendUrl = frontendUrl;
	}

	/** {@code as} identifica a pessoa fictícia (ex.: "beatriz"); chamadas repetidas voltam à mesma conta. */
	@GetMapping("/api/dev/login-as")
	void loginAs(@RequestParam String as, @RequestParam(required = false) String name, HttpServletResponse response)
			throws IOException {
		String slug = as.trim().toLowerCase();
		String displayName = StringUtils.hasText(name) ? name : slug;
		UserSummary user = userService.findOrCreateByGoogle("dev:" + slug, slug + "@dev.duo.local", displayName);
		response.addHeader(HttpHeaders.SET_COOKIE, cookies.set(refreshTokenService.issue(user.id())));
		response.sendRedirect(frontendUrl);
	}
}
