package com.duo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.duo.users.EmailAlreadyLinkedException;
import com.duo.users.UserService;
import com.duo.users.UserSummary;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleLoginSuccessHandlerTest {

	private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

	private final UserService userService = mock(UserService.class);
	private final RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
	private final RefreshCookies cookies = new RefreshCookies(true, Clock.fixed(NOW, ZoneOffset.UTC));
	private final GoogleLoginSuccessHandler handler = new GoogleLoginSuccessHandler(userService, refreshTokens,
			cookies, "http://front.test");

	private OidcUser googleUser(Boolean emailVerified) {
		OidcUser user = mock(OidcUser.class);
		when(user.getSubject()).thenReturn("sub-1");
		when(user.getEmail()).thenReturn("ana@example.com");
		when(user.getFullName()).thenReturn("Ana");
		when(user.getEmailVerified()).thenReturn(emailVerified);
		return user;
	}

	@Test
	void verifiedEmailCreatesAccountSetsRefreshCookieAndRedirectsToFrontend() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true);
		when(userService.findOrCreateByGoogle("sub-1", "ana@example.com", "Ana"))
			.thenReturn(new UserSummary(7L, "ana@example.com", "Ana"));
		when(refreshTokens.issue(7L)).thenReturn(new IssuedRefreshToken(7L, "token-secreto", NOW.plusSeconds(60)));

		handler.onAuthenticationSuccess(request, response, new TestingAuthenticationToken(googleUser(true), null));

		assertThat(response.getRedirectedUrl()).isEqualTo("http://front.test");
		String cookie = response.getHeader("Set-Cookie");
		assertThat(cookie).contains("duo_refresh=token-secreto", "HttpOnly", "Secure", "SameSite=Strict",
				"Path=/api/auth");
		assertThat(request.getSession(false)).isNull();
	}

	@Test
	void unverifiedEmailIsRejectedWithoutCreatingAccountOrSession() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
			new TestingAuthenticationToken(googleUser(false), null));

		verify(userService, never()).findOrCreateByGoogle(any(), any(), any());
		verify(refreshTokens, never()).issue(any());
		assertThat(response.getRedirectedUrl()).isEqualTo("http://front.test/?erro=email-nao-verificado");
		assertThat(response.getHeader("Set-Cookie")).isNull();
	}

	@Test
	void emailLinkedToAnotherAccountIsRejectedWithoutSession() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(userService.findOrCreateByGoogle(any(), any(), any())).thenThrow(new EmailAlreadyLinkedException());

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
			new TestingAuthenticationToken(googleUser(true), null));

		verify(refreshTokens, never()).issue(any());
		assertThat(response.getRedirectedUrl()).isEqualTo("http://front.test/?erro=email-ja-vinculado");
		assertThat(response.getHeader("Set-Cookie")).isNull();
	}
}
