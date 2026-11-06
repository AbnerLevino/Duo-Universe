package com.duo.media.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
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
 * Épico 3, Etapas 7 e 9: avatar e fotos pelos controllers de verdade. Os testes de acesso cruzado são o
 * coração: quem não pode ver uma foto recebe 404 mesmo sabendo o id (seção 15 do CLAUDE.md, RN-19/RN-20).
 * O limite de fotos é reduzido para 3 só aqui, para a cota ser testável sem enviar 200 arquivos.
 */
@ActiveProfiles("test")
@SpringBootTest(properties = "duo.media.max-photos=3")
@WebAppConfiguration
class MediaHttpTest {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4 };
	private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F' };

	@Autowired
	WebApplicationContext context;

	@Autowired
	UserService userService;

	@Autowired
	JwtEncoder encoder;

	@Autowired
	JdbcTemplate jdbc;

	MockMvc mvc;
	ObjectMapper json = new ObjectMapper();

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
			jdbc.update("update users set avatar_media_id = null where id = ?", id);
			jdbc.update("delete from media_assets where owner_user_id = ?", id);
			jdbc.update("delete from rate_limit_attempts where attempt_key = ?", id.toString());
			jdbc.update("delete from users where id = ?", id);
		}
	}

	private UserSummary newUser(String name, ProfileVisibility visibility) {
		String unique = UUID.randomUUID().toString();
		UserSummary user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", name);
		createdUserIds.add(user.id());
		userService.chooseHandle(user.id(), "m" + unique.replace("-", "").substring(0, 10));
		userService.updateProfile(user.id(), new ProfileUpdate(name, null, null, MaritalStatus.SINGLE, visibility));
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

	private long uploadPhoto(UserSummary owner, String visibility) throws Exception {
		String body = mvc
			.perform(multipart("/api/me/photos").file(new MockMultipartFile("file", "f.png", "image/png", PNG))
				.param("caption", "Praia")
				.param("visibility", visibility)
				.header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return json.readTree(body).get("id").asLong();
	}

	private void follow(UserSummary follower, UserSummary target) throws Exception {
		mvc.perform(post("/api/profiles/" + target.handle() + "/follow").header("Authorization",
				bearerFor(follower.id())));
	}

	// ---------- avatar ----------

	@Test
	void requestsWithoutATokenAreRejected() throws Exception {
		mvc.perform(get("/api/media/1")).andExpect(status().isUnauthorized());
		mvc.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "a.png", "image/png", PNG)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void uploadsAnAvatarShownInTheProfileAndServedWithSafeHeaders() throws Exception {
		UserSummary owner = newUser("Com Avatar", ProfileVisibility.PUBLIC);

		String body = mvc
			.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "a.png", "image/png", PNG))
				.header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.kind").value("AVATAR"))
			.andExpect(jsonPath("$.contentType").value("image/png"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long mediaId = json.readTree(body).get("id").asLong();

		mvc.perform(get("/api/me/profile").header("Authorization", bearerFor(owner.id())))
			.andExpect(jsonPath("$.avatarMediaId").value(mediaId));
		mvc.perform(get("/api/media/" + mediaId).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Type", "image/png"))
			.andExpect(header().string("X-Content-Type-Options", "nosniff"))
			.andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("private")))
			.andExpect(content().bytes(PNG));
	}

	@Test
	void everyoneNotBlockedSeesAnAvatarEvenOnAPrivateProfile() throws Exception {
		UserSummary owner = newUser("Privada Com Avatar", ProfileVisibility.PRIVATE);
		UserSummary stranger = newUser("Estranho", ProfileVisibility.PUBLIC);
		UserSummary blocked = newUser("Bloqueado", ProfileVisibility.PUBLIC);
		String body = mvc
			.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "a.jpg", "image/jpeg", JPEG))
				.header("Authorization", bearerFor(owner.id())))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long mediaId = json.readTree(body).get("id").asLong();
		mvc.perform(put("/api/me/blocks/" + blocked.handle()).header("Authorization", bearerFor(owner.id())));

		mvc.perform(get("/api/media/" + mediaId).header("Authorization", bearerFor(stranger.id())))
			.andExpect(status().isOk());
		mvc.perform(get("/api/media/" + mediaId).header("Authorization", bearerFor(blocked.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void refusesFilesThatAreNotRealImagesEmptyOrTooBig() throws Exception {
		UserSummary owner = newUser("Rejeita", ProfileVisibility.PUBLIC);
		String token = bearerFor(owner.id());

		// um texto com nome e tipo de imagem: o que vale são os bytes (RN-23)
		mvc.perform(multipart("/api/me/avatar")
			.file(new MockMultipartFile("file", "falso.png", "image/png", "nao sou imagem".getBytes()))
			.header("Authorization", token)).andExpect(status().isBadRequest());
		mvc.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "v.png", "image/png", new byte[0]))
			.header("Authorization", token)).andExpect(status().isBadRequest());
		byte[] big = new byte[1024 * 1024 + 1];
		System.arraycopy(PNG, 0, big, 0, PNG.length);
		mvc.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "g.png", "image/png", big))
			.header("Authorization", token)).andExpect(status().isBadRequest());
	}

	@Test
	void replacingTheAvatarDeletesTheOldOne() throws Exception {
		UserSummary owner = newUser("Troca Avatar", ProfileVisibility.PUBLIC);
		String token = bearerFor(owner.id());
		long first = json
			.readTree(mvc
				.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "1.png", "image/png", PNG))
					.header("Authorization", token))
				.andReturn()
				.getResponse()
				.getContentAsString())
			.get("id")
			.asLong();

		long second = json
			.readTree(mvc
				.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "2.jpg", "image/jpeg", JPEG))
					.header("Authorization", token))
				.andReturn()
				.getResponse()
				.getContentAsString())
			.get("id")
			.asLong();

		assertThat(second).isNotEqualTo(first);
		mvc.perform(get("/api/media/" + first).header("Authorization", token)).andExpect(status().isNotFound());
		mvc.perform(get("/api/media/" + second).header("Authorization", token)).andExpect(status().isOk());
	}

	@Test
	void removingTheAvatarClearsTheProfile() throws Exception {
		UserSummary owner = newUser("Remove Avatar", ProfileVisibility.PUBLIC);
		String token = bearerFor(owner.id());
		mvc.perform(multipart("/api/me/avatar").file(new MockMultipartFile("file", "1.png", "image/png", PNG))
			.header("Authorization", token));

		mvc.perform(delete("/api/me/avatar").header("Authorization", token)).andExpect(status().isNoContent());

		mvc.perform(get("/api/me/profile").header("Authorization", token))
			.andExpect(jsonPath("$.avatarMediaId").doesNotExist());
	}

	// ---------- fotos: acesso cruzado ----------

	@Test
	void aPhotoDefaultsToOnlyMeAndNobodyElseSeesIt() throws Exception {
		UserSummary owner = newUser("Dona Foto", ProfileVisibility.PUBLIC);
		UserSummary stranger = newUser("Intruso", ProfileVisibility.PUBLIC);
		String body = mvc
			.perform(multipart("/api/me/photos").file(new MockMultipartFile("file", "f.png", "image/png", PNG))
				.header("Authorization", bearerFor(owner.id())))
			.andExpect(jsonPath("$.visibility").value("ONLY_ME"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long id = json.readTree(body).get("id").asLong();

		mvc.perform(get("/api/media/" + id).header("Authorization", bearerFor(stranger.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/media/users/" + owner.id() + "/photos").header("Authorization", bearerFor(stranger.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
		mvc.perform(get("/api/media/users/" + owner.id() + "/photos").header("Authorization", bearerFor(owner.id())))
			.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void photoVisibilityMatrixOnAPublicProfile() throws Exception {
		UserSummary owner = newUser("Dona Matriz", ProfileVisibility.PUBLIC);
		UserSummary stranger = newUser("Visitante", ProfileVisibility.PUBLIC);
		UserSummary follower = newUser("Seguidor", ProfileVisibility.PUBLIC);
		long onlyMe = uploadPhoto(owner, "ONLY_ME");
		long forFollowers = uploadPhoto(owner, "DUOLLOWERS");
		long publicPhoto = uploadPhoto(owner, "PUBLIC");
		follow(follower, owner);

		String strangerToken = bearerFor(stranger.id());
		mvc.perform(get("/api/media/users/" + owner.id() + "/photos").header("Authorization", strangerToken))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(publicPhoto));
		mvc.perform(get("/api/media/" + forFollowers).header("Authorization", strangerToken))
			.andExpect(status().isNotFound());

		String followerToken = bearerFor(follower.id());
		mvc.perform(get("/api/media/users/" + owner.id() + "/photos").header("Authorization", followerToken))
			.andExpect(jsonPath("$.length()").value(2));
		mvc.perform(get("/api/media/" + forFollowers).header("Authorization", followerToken))
			.andExpect(status().isOk());
		mvc.perform(get("/api/media/" + onlyMe).header("Authorization", followerToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void onAPrivateProfileEvenAPublicPhotoNeedsAnApprovedFollow() throws Exception {
		UserSummary owner = newUser("Dona Privada", ProfileVisibility.PRIVATE);
		UserSummary visitor = newUser("Pedinte", ProfileVisibility.PUBLIC);
		long publicPhoto = uploadPhoto(owner, "PUBLIC");
		String visitorToken = bearerFor(visitor.id());

		mvc.perform(get("/api/media/" + publicPhoto).header("Authorization", visitorToken))
			.andExpect(status().isNotFound());

		follow(visitor, owner); // vira só um pedido
		mvc.perform(get("/api/media/" + publicPhoto).header("Authorization", visitorToken))
			.andExpect(status().isNotFound());

		mvc.perform(post("/api/me/follow-requests/" + visitor.handle() + "/accept").header("Authorization",
				bearerFor(owner.id())));
		mvc.perform(get("/api/media/" + publicPhoto).header("Authorization", visitorToken))
			.andExpect(status().isOk());
	}

	@Test
	void aBlockedPersonCannotListThePhotosOfWhoBlockedThem() throws Exception {
		UserSummary owner = newUser("Dona Bloqueio", ProfileVisibility.PUBLIC);
		UserSummary blocked = newUser("Bloqueado Foto", ProfileVisibility.PUBLIC);
		uploadPhoto(owner, "PUBLIC");
		mvc.perform(put("/api/me/blocks/" + blocked.handle()).header("Authorization", bearerFor(owner.id())));

		mvc.perform(get("/api/media/users/" + owner.id() + "/photos").header("Authorization", bearerFor(blocked.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void aPhotoHiddenByModerationIsSeenOnlyByItsOwner() throws Exception {
		UserSummary owner = newUser("Dona Moderada", ProfileVisibility.PUBLIC);
		UserSummary other = newUser("Outra", ProfileVisibility.PUBLIC);
		long id = uploadPhoto(owner, "PUBLIC");
		jdbc.update("update media_assets set hidden_at = now() where id = ?", id);

		mvc.perform(get("/api/media/" + id).header("Authorization", bearerFor(other.id())))
			.andExpect(status().isNotFound());
		mvc.perform(get("/api/media/" + id).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isOk());
	}

	// ---------- fotos: edição, exclusão, cota, denúncia ----------

	@Test
	void onlyTheOwnerEditsOrDeletesAPhoto() throws Exception {
		UserSummary owner = newUser("Dona Edita", ProfileVisibility.PUBLIC);
		UserSummary other = newUser("Outro Edita", ProfileVisibility.PUBLIC);
		long id = uploadPhoto(owner, "ONLY_ME");
		String edit = "{\"caption\":\"Nova legenda\",\"visibility\":\"PUBLIC\"}";

		mvc.perform(patch("/api/media/" + id).header("Authorization", bearerFor(other.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(edit)).andExpect(status().isNotFound());
		mvc.perform(delete("/api/media/" + id).header("Authorization", bearerFor(other.id())))
			.andExpect(status().isNotFound());

		mvc.perform(patch("/api/media/" + id).header("Authorization", bearerFor(owner.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(edit))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.caption").value("Nova legenda"))
			.andExpect(jsonPath("$.visibility").value("PUBLIC"));
		mvc.perform(delete("/api/media/" + id).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isNoContent());
		mvc.perform(get("/api/media/" + id).header("Authorization", bearerFor(owner.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void thePhotoQuotaIsEnforced() throws Exception {
		UserSummary owner = newUser("Dona Cota", ProfileVisibility.PUBLIC);
		for (int i = 0; i < 3; i++) {
			uploadPhoto(owner, "ONLY_ME");
		}

		mvc.perform(multipart("/api/me/photos").file(new MockMultipartFile("file", "f.png", "image/png", PNG))
			.header("Authorization", bearerFor(owner.id()))).andExpect(status().isConflict());
	}

	@Test
	void aLongCaptionIsRefused() throws Exception {
		UserSummary owner = newUser("Legenda Longa", ProfileVisibility.PUBLIC);

		mvc.perform(multipart("/api/me/photos").file(new MockMultipartFile("file", "f.png", "image/png", PNG))
			.param("caption", "x".repeat(201))
			.header("Authorization", bearerFor(owner.id()))).andExpect(status().isBadRequest());
	}

	@Test
	void reportingAPhotoNeedsToSeeItFirst() throws Exception {
		UserSummary owner = newUser("Dona Denuncia", ProfileVisibility.PUBLIC);
		UserSummary reporter = newUser("Denunciante", ProfileVisibility.PUBLIC);
		UserSummary outsider = newUser("Sem Acesso", ProfileVisibility.PUBLIC);
		long publicPhoto = uploadPhoto(owner, "PUBLIC");
		long privatePhoto = uploadPhoto(owner, "ONLY_ME");
		String body = "{\"reason\":\"NUDITY_OR_SEXUAL\"}";

		mvc.perform(post("/api/media/" + publicPhoto + "/report").header("Authorization", bearerFor(reporter.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)).andExpect(status().isNoContent());
		// denúncia repetida é inofensiva
		mvc.perform(post("/api/media/" + publicPhoto + "/report").header("Authorization", bearerFor(reporter.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)).andExpect(status().isNoContent());
		// não dá para denunciar o que não se pode ver
		mvc.perform(post("/api/media/" + privatePhoto + "/report").header("Authorization", bearerFor(outsider.id()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)).andExpect(status().isNotFound());

		Integer stored = jdbc.queryForObject("select count(*) from reports where target_media_id = ?", Integer.class,
				publicPhoto);
		assertThat(stored).isEqualTo(1);
	}
}
