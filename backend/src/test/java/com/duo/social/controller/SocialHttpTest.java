package com.duo.social.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;
import com.duo.users.UserService;
import com.duo.users.dto.ProfileUpdate;
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

/**
 * Épico 3, Etapas 3 a 6: o perfil visto por outras pessoas, a busca, Duollow, bloqueio e denúncia passando
 * pelos controllers de verdade. Testes de <strong>acesso cruzado</strong> (seção 15 do CLAUDE.md): quem não deve
 * ver, não vê, mesmo sabendo o DuoName. Sem {@code @Transactional} (cada chamada HTTP confirma a própria
 * transação), então cada teste limpa o que criou no {@code @AfterEach}.
 */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
class SocialHttpTest {

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
			jdbc.update("delete from reports where reporter_id = ? or target_user_id = ?", id, id);
			jdbc.update("delete from follows where follower_id = ? or followee_id = ?", id, id);
			jdbc.update("delete from blocks where blocker_id = ? or blocked_id = ?", id, id);
			jdbc.update("delete from rate_limit_attempts where attempt_key = ?", id.toString());
			jdbc.update("delete from users where id = ?", id);
		}
	}

	private UserSummary newUser(String name, ProfileVisibility visibility) {
		String unique = UUID.randomUUID().toString();
		UserSummary user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", name);
		createdUserIds.add(user.id());
		userService.chooseHandle(user.id(), "u" + unique.replace("-", "").substring(0, 10));
		userService.updateProfile(user.id(),
				new ProfileUpdate(name, "bio de " + name, "Dev", MaritalStatus.SINGLE, visibility));
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

	// ---------- ver perfil ----------

	@Test
	void requestsWithoutATokenAreRejected() throws Exception {
		mvc.perform(get("/api/profiles/qualquer")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/search/profiles").param("q", "abc")).andExpect(status().isUnauthorized());
	}

	@Test
	void aStrangerSeesAPublicProfileCardAndContentAccess() throws Exception {
		UserSummary owner = newUser("Dona Publica", ProfileVisibility.PUBLIC);
		UserSummary stranger = newUser("Visitante", ProfileVisibility.PUBLIC);

		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", bearerFor(stranger.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.displayName").value("Dona Publica"))
			.andExpect(jsonPath("$.bio").value("bio de Dona Publica"))
			.andExpect(jsonPath("$.canSeeContent").value(true))
			.andExpect(jsonPath("$.self").value(false))
			.andExpect(jsonPath("$.relation").value("NONE"))
			.andExpect(jsonPath("$.email").doesNotExist());
	}

	@Test
	void aPrivateProfileShowsOnlyTheCardToStrangersAndOpensToApprovedFollowers() throws Exception {
		UserSummary owner = newUser("Dona Privada", ProfileVisibility.PRIVATE);
		UserSummary visitor = newUser("Curioso", ProfileVisibility.PUBLIC);
		String ownerToken = bearerFor(owner.id());
		String visitorToken = bearerFor(visitor.id());

		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", visitorToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.displayName").value("Dona Privada"))
			.andExpect(jsonPath("$.canSeeContent").value(false));

		// pede para seguir: vira um pedido, não um Duollow
		mvc.perform(post("/api/profiles/" + owner.handle() + "/follow").header("Authorization", visitorToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.relation").value("REQUESTED"));
		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", visitorToken))
			.andExpect(jsonPath("$.canSeeContent").value(false))
			.andExpect(jsonPath("$.relation").value("REQUESTED"));

		// a dona vê o pedido e aceita
		mvc.perform(get("/api/me/follow-requests").header("Authorization", ownerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].handle").value(visitor.handle()));
		mvc.perform(post("/api/me/follow-requests/" + visitor.handle() + "/accept").header("Authorization", ownerToken))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", visitorToken))
			.andExpect(jsonPath("$.canSeeContent").value(true))
			.andExpect(jsonPath("$.relation").value("FOLLOWING"))
			.andExpect(jsonPath("$.duollowers").value(1));
		mvc.perform(get("/api/me/duollowers").header("Authorization", ownerToken))
			.andExpect(jsonPath("$[0].handle").value(visitor.handle()));
		mvc.perform(get("/api/me/duollowing").header("Authorization", visitorToken))
			.andExpect(jsonPath("$[0].handle").value(owner.handle()));
	}

	@Test
	void aSilentlyRejectedRequestLeavesNoTrace() throws Exception {
		UserSummary owner = newUser("Dona Recusa", ProfileVisibility.PRIVATE);
		UserSummary visitor = newUser("Pedinte", ProfileVisibility.PUBLIC);
		mvc.perform(post("/api/profiles/" + owner.handle() + "/follow").header("Authorization", bearerFor(visitor.id())));

		mvc.perform(delete("/api/me/follow-requests/" + visitor.handle()).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", bearerFor(visitor.id())))
			.andExpect(jsonPath("$.relation").value("NONE"));
		mvc.perform(get("/api/me/follow-requests").header("Authorization", bearerFor(owner.id())))
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void followingAPublicProfileIsImmediateAndCanBeUndone() throws Exception {
		UserSummary owner = newUser("Dona Aberta", ProfileVisibility.PUBLIC);
		UserSummary fan = newUser("Fa", ProfileVisibility.PUBLIC);
		String fanToken = bearerFor(fan.id());

		mvc.perform(post("/api/profiles/" + owner.handle() + "/follow").header("Authorization", fanToken))
			.andExpect(jsonPath("$.relation").value("FOLLOWING"));
		// repetir é inofensivo
		mvc.perform(post("/api/profiles/" + owner.handle() + "/follow").header("Authorization", fanToken))
			.andExpect(jsonPath("$.relation").value("FOLLOWING"));
		mvc.perform(delete("/api/profiles/" + owner.handle() + "/follow").header("Authorization", fanToken))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/profiles/" + owner.handle()).header("Authorization", fanToken))
			.andExpect(jsonPath("$.relation").value("NONE"))
			.andExpect(jsonPath("$.duollowers").value(0));
	}

	@Test
	void anOwnerCanRemoveAFollower() throws Exception {
		UserSummary owner = newUser("Dona Remove", ProfileVisibility.PUBLIC);
		UserSummary fan = newUser("Fa Removido", ProfileVisibility.PUBLIC);
		mvc.perform(post("/api/profiles/" + owner.handle() + "/follow").header("Authorization", bearerFor(fan.id())));

		mvc.perform(delete("/api/me/duollowers/" + fan.handle()).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/me/duollowers").header("Authorization", bearerFor(owner.id())))
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void youCannotFollowYourselfOrAnUnknownPerson() throws Exception {
		UserSummary me = newUser("Eu Mesmo", ProfileVisibility.PUBLIC);

		mvc.perform(post("/api/profiles/" + me.handle() + "/follow").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isBadRequest());
		mvc.perform(post("/api/profiles/ninguem_assim_existe/follow").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void acceptingARequestThatDoesNotExistIsRejected() throws Exception {
		UserSummary owner = newUser("Dona Sem Pedido", ProfileVisibility.PRIVATE);
		UserSummary other = newUser("Outro", ProfileVisibility.PUBLIC);

		mvc.perform(post("/api/me/follow-requests/" + other.handle() + "/accept")
			.header("Authorization", bearerFor(owner.id()))).andExpect(status().isNotFound());
	}

	// ---------- bloqueio e moderação ----------

	@Test
	void blockingHidesBothSidesAndRemovesFollows() throws Exception {
		UserSummary a = newUser("Bloqueadora", ProfileVisibility.PUBLIC);
		UserSummary b = newUser("Bloqueado", ProfileVisibility.PUBLIC);
		mvc.perform(post("/api/profiles/" + a.handle() + "/follow").header("Authorization", bearerFor(b.id())));
		mvc.perform(post("/api/profiles/" + b.handle() + "/follow").header("Authorization", bearerFor(a.id())));

		mvc.perform(put("/api/me/blocks/" + b.handle()).header("Authorization", bearerFor(a.id())))
			.andExpect(status().isNoContent());

		// nos dois sentidos o perfil "não existe", com a mesma resposta de um perfil inexistente
		mvc.perform(get("/api/profiles/" + b.handle()).header("Authorization", bearerFor(a.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/profiles/" + a.handle()).header("Authorization", bearerFor(b.id())))
			.andExpect(status().isNotFound());
		// o bloqueado não consegue pedir para seguir de novo
		mvc.perform(post("/api/profiles/" + a.handle() + "/follow").header("Authorization", bearerFor(b.id())))
			.andExpect(status().isNotFound());
		// os Duollows dos dois lados foram desfeitos
		mvc.perform(get("/api/me/duollowers").header("Authorization", bearerFor(a.id())))
			.andExpect(jsonPath("$").isEmpty());
		mvc.perform(get("/api/me/duollowing").header("Authorization", bearerFor(a.id())))
			.andExpect(jsonPath("$").isEmpty());
		mvc.perform(get("/api/me/blocks").header("Authorization", bearerFor(a.id())))
			.andExpect(jsonPath("$[0].handle").value(b.handle()));
	}

	@Test
	void unblockingBringsTheProfileBack() throws Exception {
		UserSummary a = newUser("Desbloqueadora", ProfileVisibility.PUBLIC);
		UserSummary b = newUser("Desbloqueado", ProfileVisibility.PUBLIC);
		mvc.perform(put("/api/me/blocks/" + b.handle()).header("Authorization", bearerFor(a.id())));

		mvc.perform(delete("/api/me/blocks/" + b.handle()).header("Authorization", bearerFor(a.id())))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/profiles/" + b.handle()).header("Authorization", bearerFor(a.id())))
			.andExpect(status().isOk());
	}

	@Test
	void aHiddenProfileDisappearsForOthersButNotForItsOwner() throws Exception {
		UserSummary hidden = newUser("Oculta Pela Moderacao", ProfileVisibility.PUBLIC);
		UserSummary other = newUser("Outra Pessoa", ProfileVisibility.PUBLIC);
		jdbc.update("update users set hidden_at = now() where id = ?", hidden.id());

		mvc.perform(get("/api/profiles/" + hidden.handle()).header("Authorization", bearerFor(other.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/profiles/" + hidden.handle()).header("Authorization", bearerFor(hidden.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.self").value(true));
	}

	// ---------- busca ----------

	@Test
	void searchFindsByNameIgnoringAccentsAndByDuoName() throws Exception {
		String token = "Zq" + UUID.randomUUID().toString().substring(0, 6);
		UserSummary target = newUser("João " + token, ProfileVisibility.PRIVATE);
		UserSummary searcher = newUser("Quem Busca", ProfileVisibility.PUBLIC);

		mvc.perform(get("/api/search/profiles").param("q", "joao " + token.toLowerCase())
			.header("Authorization", bearerFor(searcher.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].profile.handle").value(target.handle()))
			.andExpect(jsonPath("$.items[0].relation").value("NONE"));
		// perfil privado também é achado, por nome ou por DuoName
		mvc.perform(get("/api/search/profiles").param("q", target.handle())
			.header("Authorization", bearerFor(searcher.id())))
			.andExpect(jsonPath("$.items[0].profile.userId").value(target.id()));
	}

	@Test
	void searchNeedsThreeLettersAndNeverReturnsYourselfOrBlockedPeople() throws Exception {
		String token = "Wx" + UUID.randomUUID().toString().substring(0, 6);
		UserSummary me = newUser("Eu " + token, ProfileVisibility.PUBLIC);
		UserSummary blocked = newUser("Bloq " + token, ProfileVisibility.PUBLIC);
		UserSummary visible = newUser("Visivel " + token, ProfileVisibility.PUBLIC);
		mvc.perform(put("/api/me/blocks/" + blocked.handle()).header("Authorization", bearerFor(me.id())));

		mvc.perform(get("/api/search/profiles").param("q", "ab").header("Authorization", bearerFor(me.id())))
			.andExpect(status().isBadRequest());
		mvc.perform(get("/api/search/profiles").param("q", token).header("Authorization", bearerFor(me.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(1))
			.andExpect(jsonPath("$.items[0].profile.userId").value(visible.id()));
		// quem foi bloqueado também não enxerga quem bloqueou
		mvc.perform(get("/api/search/profiles").param("q", token).header("Authorization", bearerFor(blocked.id())))
			.andExpect(jsonPath("$.items.length()").value(1))
			.andExpect(jsonPath("$.items[0].profile.userId").value(visible.id()));
	}

	// ---------- denúncia ----------

	@Test
	void reportingAProfileIsAcceptedAndRepeatingItIsHarmless() throws Exception {
		UserSummary target = newUser("Denunciada", ProfileVisibility.PUBLIC);
		UserSummary reporter = newUser("Denunciante", ProfileVisibility.PUBLIC);
		String body = "{\"reason\":\"SPAM\",\"details\":null}";

		for (int i = 0; i < 2; i++) {
			mvc.perform(post("/api/profiles/" + target.handle() + "/report")
				.header("Authorization", bearerFor(reporter.id()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isNoContent());
		}

		Integer stored = jdbc.queryForObject("select count(*) from reports where reporter_id = ?", Integer.class,
				reporter.id());
		assertThat(stored).isEqualTo(1);
	}

	@Test
	void aReportNeedsAReasonAndOtherNeedsText() throws Exception {
		UserSummary target = newUser("Alvo", ProfileVisibility.PUBLIC);
		UserSummary reporter = newUser("Reporter", ProfileVisibility.PUBLIC);

		mvc.perform(post("/api/profiles/" + target.handle() + "/report")
			.header("Authorization", bearerFor(reporter.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"OTHER\",\"details\":\"  \"}")).andExpect(status().isBadRequest());
		mvc.perform(post("/api/profiles/" + target.handle() + "/report")
			.header("Authorization", bearerFor(reporter.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"details\":\"sem motivo\"}")).andExpect(status().isBadRequest());
		mvc.perform(post("/api/profiles/" + reporter.handle() + "/report")
			.header("Authorization", bearerFor(reporter.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"SPAM\"}")).andExpect(status().isBadRequest());
	}

	@Test
	void youCannotReportSomeoneYouCannotSee() throws Exception {
		UserSummary target = newUser("Escondida", ProfileVisibility.PUBLIC);
		UserSummary blocker = newUser("Quem Bloqueou", ProfileVisibility.PUBLIC);
		mvc.perform(put("/api/me/blocks/" + blocker.handle()).header("Authorization", bearerFor(target.id())));

		mvc.perform(post("/api/profiles/" + target.handle() + "/report")
			.header("Authorization", bearerFor(blocker.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"SPAM\"}")).andExpect(status().isNotFound());
	}
}
