package com.duo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.duo.users.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleLoginSuccessHandlerTest {

	private final UserService userService = mock(UserService.class);
	private final GoogleLoginSuccessHandler handler = new GoogleLoginSuccessHandler(userService);

	private OidcUser googleUser(Boolean emailVerified) {
		OidcUser user = mock(OidcUser.class);
		when(user.getSubject()).thenReturn("sub-1");
		when(user.getEmail()).thenReturn("ana@example.com");
		when(user.getFullName()).thenReturn("Ana");
		when(user.getEmailVerified()).thenReturn(emailVerified);
		return user;
	}

	@Test
	void verifiedEmailCreatesAccountAndRedirects() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
			new TestingAuthenticationToken(googleUser(true), null));

		verify(userService).findOrCreateByGoogle("sub-1", "ana@example.com", "Ana");
		assertThat(response.getRedirectedUrl()).isEqualTo("/api/me");
	}

	@Test
	void unverifiedEmailIsRejectedWithoutCreatingAccount() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
			new TestingAuthenticationToken(googleUser(false), null));

		verify(userService, never()).findOrCreateByGoogle(any(), any(), any());
		assertThat(response.getStatus()).isEqualTo(403);
	}
}
