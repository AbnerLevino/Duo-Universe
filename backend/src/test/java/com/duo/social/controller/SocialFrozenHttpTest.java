package com.duo.social.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * RF-A11: com a camada social congelada ({@code duo.features.social.enabled=false}, o padrão do produto),
 * busca, Duollowers e perfil/fotos de terceiros não respondem; bloqueio continua funcionando e as fotos da
 * própria pessoa também.
 */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
@TestPropertySource(properties = "duo.features.social.enabled=false")
class SocialFrozenHttpTest {

	@Autowired
	WebApplicationContext context;

	@Autowired
	UserService userService;

	@Autowired
	JwtEncoder encoder;

	@Autowired
	JdbcTemplate jdbc;

	MockMvc mvc;

	private final List<Long> createdUserIds = new ArrayList<>();

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@AfterEach
	void cleanUp() {
		for (Long id : createdUserIds) {
			jdbc.update("delete from blocks where blocker_id = ? or blocked_id = ?", id, id);
			jdbc.update("delete from users where id = ?", id);
		}
	}

	private UserSummary newUser() {
		String unique = UUID.randomUUID().toString();
		UserSummary user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", "Pessoa");
		createdUserIds.add(user.id());
		userService.chooseHandle(user.id(), "u" + unique.replace("-", "").substring(0, 10));
		return userService.findById(user.id()).orElseThrow();
	}

	private String bearerFor(Long userId) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("duo")
			.subject(userId.toString())
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(900))
			.build();
		return "Bearer " + encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();
	}

	@Test
	void searchFollowAndProfileViewDoNotExist() throws Exception {
		UserSummary me = newUser();
		UserSummary other = newUser();

		mvc.perform(get("/api/search/profiles").param("q", "abc").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/profiles/" + other.handle()).header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
		mvc.perform(post("/api/profiles/" + other.handle() + "/follow").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/me/duollowers").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/me/follow-requests").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void photosOfAnotherPersonAreNotReachable() throws Exception {
		UserSummary me = newUser();
		UserSummary other = newUser();

		mvc.perform(get("/api/media/users/" + other.id() + "/photos").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void ownPhotosListStillWorks() throws Exception {
		UserSummary me = newUser();

		mvc.perform(get("/api/media/users/" + me.id() + "/photos").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isOk());
	}

	@Test
	void blockingStillWorks() throws Exception {
		UserSummary me = newUser();
		UserSummary other = newUser();

		mvc.perform(put("/api/me/blocks/" + other.handle()).header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNoContent());
	}
}
