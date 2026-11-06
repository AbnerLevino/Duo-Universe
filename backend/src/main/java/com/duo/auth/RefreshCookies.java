package com.duo.auth;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.duo.auth.dto.IssuedRefreshToken;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * O cookie do token de renovação (RA-04): {@code HttpOnly} (o JavaScript da página não lê),
 * {@code SameSite=Strict} (outros sites não o fazem ser enviado) e restrito ao caminho {@code /api/auth}.
 * {@code Secure} fica ligado por padrão; só se desliga em desenvolvimento sem HTTPS.
 */
@Component
public class RefreshCookies {

	static final String NAME = "duo_refresh";
	static final String PATH = "/api/auth";

	private final boolean secure;
	private final Clock clock;

	RefreshCookies(@Value("${duo.auth.cookie-secure:true}") boolean secure, Clock clock) {
		this.secure = secure;
		this.clock = clock;
	}

	public String set(IssuedRefreshToken issued) {
		return build(issued.token(), Duration.between(clock.instant(), issued.expiresAt())).toString();
	}

	public String clear() {
		return build("", Duration.ZERO).toString();
	}

	public Optional<String> read(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return Optional.empty();
		}
		return Arrays.stream(request.getCookies())
			.filter(cookie -> NAME.equals(cookie.getName()))
			.map(Cookie::getValue)
			.filter(value -> !value.isBlank())
			.findFirst();
	}

	private ResponseCookie build(String value, Duration maxAge) {
		return ResponseCookie.from(NAME, value)
			.httpOnly(true)
			.secure(secure)
			.sameSite("Strict")
			.path(PATH)
			.maxAge(maxAge)
			.build();
	}
}
