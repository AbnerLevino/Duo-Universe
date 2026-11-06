package com.duo.auth;

import java.io.IOException;

import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Roda quando o Google confirma a identidade (DEC-030): garante que a conta existe no Duo, abre a sessão
 * própria (cookie do token de renovação, RA-04) e devolve a pessoa ao frontend. O token de acesso nunca
 * viaja na URL: o frontend o pede em {@code POST /api/auth/refresh}. A sessão do Spring serviu só para
 * guardar o estado do login com o Google e é descartada aqui.
 */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final RefreshCookies cookies;
	private final String frontendUrl;

	GoogleLoginSuccessHandler(UserService userService, RefreshTokenService refreshTokenService,
			RefreshCookies cookies, @Value("${duo.frontend-url}") String frontendUrl) {
		this.userService = userService;
		this.refreshTokenService = refreshTokenService;
		this.cookies = cookies;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		discardSpringSession(request);
		if (!(authentication.getPrincipal() instanceof OidcUser google)
				|| !Boolean.TRUE.equals(google.getEmailVerified())) {
			// RA-01: só e-mail confirmado entra
			response.sendRedirect(frontendUrl + "/?erro=email-nao-verificado");
			return;
		}
		UserSummary user;
		try {
			user = userService.findOrCreateByGoogle(google.getSubject(), google.getEmail(), google.getFullName());
		}
		catch (EmailAlreadyLinkedException e) {
			response.sendRedirect(frontendUrl + "/?erro=email-ja-vinculado");
			return;
		}
		response.addHeader(HttpHeaders.SET_COOKIE, cookies.set(refreshTokenService.issue(user.id())));
		response.sendRedirect(frontendUrl);
	}

	private static void discardSpringSession(HttpServletRequest request) {
		if (request.getSession(false) != null) {
			request.getSession(false).invalidate();
		}
		SecurityContextHolder.clearContext();
	}
}
