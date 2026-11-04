package com.duo.auth;

import java.time.Clock;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A sessão do Duo (RA-04, RA-05, H4). {@code refresh} e {@code logout} são públicos porque quem chama tem,
 * no máximo, um token de acesso vencido: a prova de identidade é o cookie do token de renovação.
 * Como esse cookie é {@code SameSite=Strict}, outros sites não conseguem usá-los (por isso o CSRF do Spring
 * fica desligado).
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final RefreshTokenService refreshTokens;
	private final AccessTokenService accessTokens;
	private final RefreshCookies cookies;
	private final Clock clock;

	AuthController(RefreshTokenService refreshTokens, AccessTokenService accessTokens, RefreshCookies cookies,
			Clock clock) {
		this.refreshTokens = refreshTokens;
		this.accessTokens = accessTokens;
		this.cookies = cookies;
		this.clock = clock;
	}

	record TokenResponse(String accessToken, long expiresInSeconds) {

		@Override
		public String toString() {
			return "TokenResponse[expiresInSeconds=" + expiresInSeconds + "]";
		}
	}

	/** Troca o token de renovação por outro (rotação, RA-05) e entrega um token de acesso novo. */
	@PostMapping("/refresh")
	ResponseEntity<TokenResponse> refresh(HttpServletRequest request) {
		return cookies.read(request)
			.flatMap(refreshTokens::rotate)
			.map(next -> {
				AccessToken access = accessTokens.issue(next.userId());
				long seconds = Duration.between(clock.instant(), access.expiresAt()).toSeconds();
				return ResponseEntity.ok()
					.header(HttpHeaders.SET_COOKIE, cookies.set(next))
					.cacheControl(CacheControl.noStore())
					.body(new TokenResponse(access.value(), seconds));
			})
			.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.header(HttpHeaders.SET_COOKIE, cookies.clear())
				.build());
	}

	/** "Sair deste aparelho" (H4). */
	@PostMapping("/logout")
	ResponseEntity<Void> logout(HttpServletRequest request) {
		cookies.read(request).ifPresent(refreshTokens::revoke);
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear()).build();
	}

	/** "Sair de todos os aparelhos" (H4): exige estar autenticado, pois afeta todas as sessões. */
	@PostMapping("/logout-all")
	ResponseEntity<Void> logoutAll(@AuthenticationPrincipal Jwt jwt) {
		refreshTokens.revokeAll(Long.valueOf(jwt.getSubject()));
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear()).build();
	}
}
