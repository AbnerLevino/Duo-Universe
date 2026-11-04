package com.duo.auth;

import java.io.IOException;

import com.duo.users.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Roda quando o Google confirma a identidade (DEC-030): garante que a conta existe no Duo.
 * Provisório: a sessão ainda é a padrão do Spring; a etapa 5 a troca pelos tokens do RA-04/RA-05.
 */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserService userService;

	GoogleLoginSuccessHandler(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		if (!(authentication.getPrincipal() instanceof OidcUser google)
				|| !Boolean.TRUE.equals(google.getEmailVerified())) {
			// RA-01: só e-mail confirmado entra; desfaz o login que o Spring acabou de registrar
			request.getSession().invalidate();
			SecurityContextHolder.clearContext();
			response.sendError(HttpStatus.FORBIDDEN.value());
			return;
		}
		userService.findOrCreateByGoogle(google.getSubject(), google.getEmail(), google.getFullName());
		response.sendRedirect("/api/me");
	}
}
