package com.duo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import com.duo.users.UserService;
import com.duo.users.UserSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.spec.SecretKeySpec;

/**
 * O fluxo completo da sessão, passando por todos os filtros de segurança (RA-04, RA-05, RN-20, H4).
 * Roda numa transação que é desfeita no fim: não deixa contas nem tokens no banco de testes.
 */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
@Transactional
class SessionFlowTest {

	private static final String SECRET = "segredo-de-teste-com-mais-de-32-caracteres-123456";

	@Autowired
	WebApplicationContext context;

	@Autowired
	UserService userService;

	@Autowired
	RefreshTokenService refreshTokens;

	@Autowired
	JwtEncoder encoder;

	MockMvc mvc;
	UserSummary user;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
		String unique = UUID.randomUUID().toString();
		user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", "Pessoa Teste");
	}

	private Cookie refreshCookie(String token) {
		return new Cookie("duo_refresh", token);
	}

	private String cookieValue(MvcResult result) {
		String header = result.getResponse().getHeader("Set-Cookie");
		assertThat(header).isNotNull();
		return header.split(";")[0].split("=", 2)[1];
	}

	private String accessTokenFrom(MvcResult result) throws Exception {
		JsonNode body = new ObjectMapper().readTree(result.getResponse().getContentAsString());
		return body.get("accessToken").asText();
	}

	private String jwtFor(JwtEncoder jwtEncoder, Long userId, Instant issuedAt, Instant expiresAt, String issuer) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.subject(userId.toString())
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();
	}

	@Test
	void refreshWithoutCookieIsUnauthorized() throws Exception {
		mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
	}

	@Test
	void refreshRotatesTheCookieAndReturnsAnAccessTokenThatOpensMe() throws Exception {
		String oldToken = refreshTokens.issue(user.id()).token();

		MvcResult result = mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(oldToken)))
			.andExpect(status().isOk())
			.andReturn();
		// RA-04: 15 minutos (o JWT guarda segundos inteiros, então pode vir 1 a menos)
		long expiresIn = new ObjectMapper().readTree(result.getResponse().getContentAsString())
			.get("expiresInSeconds").asLong();
		assertThat(expiresIn).isBetween(898L, 900L);

		String setCookie = result.getResponse().getHeader("Set-Cookie");
		assertThat(setCookie).contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/api/auth");
		assertThat(cookieValue(result)).isNotEqualTo(oldToken);
		assertThat(result.getResponse().getHeader("Cache-Control")).contains("no-store");

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + accessTokenFrom(result)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(user.id()))
			.andExpect(jsonPath("$.email").value(user.email()));
	}

	@Test
	void reusingAnOldRefreshTokenEndsEveryDeviceSession() throws Exception {
		String first = refreshTokens.issue(user.id()).token();
		MvcResult rotated = mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(first)))
			.andExpect(status().isOk())
			.andReturn();
		String second = cookieValue(rotated);

		// o token antigo reaparece (foi copiado): nega e derruba a família
		MvcResult reuse = mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(first)))
			.andExpect(status().isUnauthorized())
			.andReturn();
		assertThat(reuse.getResponse().getHeader("Set-Cookie")).contains("Max-Age=0");

		mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(second))).andExpect(status().isUnauthorized());
	}

	@Test
	void meRequiresAnAccessToken() throws Exception {
		mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/me").header("Authorization", "Bearer lixo")).andExpect(status().isUnauthorized());
	}

	@Test
	void expiredAccessTokenIsRejected() throws Exception {
		Instant longAgo = Instant.now().minusSeconds(3600);
		String expired = jwtFor(encoder, user.id(), longAgo, longAgo.plusSeconds(900), "duo");

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
	}

	@Test
	void accessTokenSignedWithAnotherSecretIsRejected() throws Exception {
		JwtEncoder attacker = new NimbusJwtEncoder(new ImmutableSecret<>(
				new SecretKeySpec("outro-segredo-com-mais-de-32-caracteres-999".getBytes(StandardCharsets.UTF_8),
						"HmacSHA256")));
		String forged = jwtFor(attacker, user.id(), Instant.now(), Instant.now().plusSeconds(900), "duo");

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + forged)).andExpect(status().isUnauthorized());
	}

	@Test
	void accessTokenFromAnotherIssuerIsRejected() throws Exception {
		String other = jwtFor(encoder, user.id(), Instant.now(), Instant.now().plusSeconds(900), "outro-emissor");

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + other)).andExpect(status().isUnauthorized());
	}

	@Test
	void logoutEndsThisDeviceAndClearsTheCookie() throws Exception {
		String token = refreshTokens.issue(user.id()).token();

		MvcResult result = mvc.perform(post("/api/auth/logout").cookie(refreshCookie(token)))
			.andExpect(status().isNoContent())
			.andReturn();

		assertThat(result.getResponse().getHeader("Set-Cookie")).contains("Max-Age=0");
		mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(token))).andExpect(status().isUnauthorized());
	}

	@Test
	void logoutWithoutCookieStillSucceeds() throws Exception {
		mvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
	}

	@Test
	void logoutAllRequiresAuthenticationAndEndsEveryDevice() throws Exception {
		String phone = refreshTokens.issue(user.id()).token();
		String notebook = refreshTokens.issue(user.id()).token();
		MvcResult phoneRefresh = mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(phone)))
			.andExpect(status().isOk())
			.andReturn();
		String access = accessTokenFrom(phoneRefresh);
		String phoneNext = cookieValue(phoneRefresh);

		mvc.perform(post("/api/auth/logout-all")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/auth/logout-all").header("Authorization", "Bearer " + access))
			.andExpect(status().isNoContent());

		mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(phoneNext))).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(notebook))).andExpect(status().isUnauthorized());
	}
}
