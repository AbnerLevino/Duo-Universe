package com.duo.users.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Épico 3, Etapa 2: ver e editar o próprio perfil e trocar o DuoName pela API. */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
class UserProfileHttpTest {

	@Autowired
	WebApplicationContext context;

	@Autowired
	UserService userService;

	@Autowired
	JwtEncoder encoder;

	@Autowired
	JdbcTemplate jdbc;

	MockMvc mvc;
	UserSummary user;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
		String unique = UUID.randomUUID().toString();
		user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", "Perfil Http");
	}

	@AfterEach
	void cleanUp() {
		jdbc.update("delete from users where id = ?", user.id());
	}

	private String bearer() {
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("duo")
			.subject(user.id().toString())
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(900))
			.build();
		return "Bearer " + encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();
	}

	@Test
	void requiresAToken() throws Exception {
		mvc.perform(get("/api/me/profile")).andExpect(status().isUnauthorized());
	}

	@Test
	void aNewProfileStartsPublicAndSingleAndNeverExposesTheEmail() throws Exception {
		mvc.perform(get("/api/me/profile").header("Authorization", bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.profileVisibility").value("PUBLIC"))
			.andExpect(jsonPath("$.maritalStatus").value("SINGLE"))
			.andExpect(jsonPath("$.email").doesNotExist());
	}

	@Test
	void updatesTheProfileAndRejectsInvalidValues() throws Exception {
		mvc.perform(put("/api/me/profile").header("Authorization", bearer())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"displayName":"Nova Pessoa","bio":"Oi","profession":"Dev","maritalStatus":"DATING",
					 "profileVisibility":"PRIVATE"}"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.displayName").value("Nova Pessoa"))
			.andExpect(jsonPath("$.maritalStatus").value("DATING"))
			.andExpect(jsonPath("$.profileVisibility").value("PRIVATE"));

		mvc.perform(put("/api/me/profile").header("Authorization", bearer())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"displayName":"X","maritalStatus":"SINGLE","profileVisibility":"PUBLIC"}"""))
			.andExpect(status().isBadRequest());
		mvc.perform(put("/api/me/profile").header("Authorization", bearer())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"displayName":"Valido","maritalStatus":"INEXISTENTE","profileVisibility":"PUBLIC"}"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void theThirdHandleChangeOfTheDayIsRefusedWith429() throws Exception {
		for (String handle : new String[] { "primeiro_h", "segundo_h", "terceiro_h" }) {
			mvc.perform(put("/api/me/handle").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"handle\":\"" + handle + UUID.randomUUID().toString().substring(0, 4) + "\"}"))
				.andExpect(status().isOk());
		}

		mvc.perform(put("/api/me/handle").header("Authorization", bearer())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"handle\":\"quarto_h" + UUID.randomUUID().toString().substring(0, 4) + "\"}"))
			.andExpect(status().isTooManyRequests());
	}
}
