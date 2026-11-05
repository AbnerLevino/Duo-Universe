package com.duo.relationships;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.duo.relationships.internal.CoupleMemberRepository;
import com.duo.relationships.internal.CoupleRepository;
import com.duo.relationships.internal.CouplePairingRepository;
import com.duo.relationships.internal.CouplePairingStoryRepository;
import com.duo.users.UserService;
import com.duo.users.UserSummary;
import com.duo.users.internal.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * Etapa 13: o fluxo do vínculo passando pelos controllers de verdade, com o {@code GlobalExceptionHandler}
 * traduzindo os erros. Testes de acesso cruzado (RN-12/RN-20) — quem não participa de um pareamento ou casal
 * nunca enxerga, mesmo sabendo o id.
 *
 * <p><strong>Sem {@code @Transactional} de propósito</strong> (diferente do {@code SessionFlowTest}): o
 * {@code confirm()} aciona o {@code CoupleFactory}, que roda numa transação própria e independente
 * (REQUIRES_NEW, Etapa 8) — numa conexão de banco separada. Se o teste inteiro estivesse dentro de uma
 * transação ainda não commitada, essa conexão separada não enxergaria os usuários criados no próprio teste,
 * e a inserção falharia por violação de chave estrangeira (traduzida, por engano, como "já está em outro
 * vínculo"). Mesma lição do {@code CouplePairingConcurrencyTest}. Por isso cada teste limpa manualmente o
 * que criou, no {@code @AfterEach}.
 */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
class RelationshipsHttpTest {

	@Autowired
	WebApplicationContext context;

	@Autowired
	UserService userService;

	@Autowired
	JwtEncoder encoder;

	@Autowired
	CouplePairingRepository pairings;

	@Autowired
	CoupleRepository couples;

	@Autowired
	CoupleMemberRepository coupleMembers;

	@Autowired
	CouplePairingStoryRepository stories;

	@Autowired
	UserRepository users;

	@Autowired
	PlatformTransactionManager transactionManager;

	MockMvc mvc;
	ObjectMapper json = new ObjectMapper();

	private final List<Long> createdUserIds = new ArrayList<>();
	private final List<Long> createdPairingIds = new ArrayList<>();
	private final List<Long> createdCoupleIds = new ArrayList<>();

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	/** Precisa da própria transação para o {@code deleteAllByPairingId}: diferente do corpo do teste, os
	 * dados aqui já estão todos de verdade commitados (cada chamada HTTP confirmou a própria transação), então
	 * isso não reabre o problema de visibilidade entre conexões descrito acima. {@code @Transactional} num
	 * método de {@code @AfterEach} não é interceptado pelo Spring (esse mecanismo só olha o método de
	 * {@code @Test}) — por isso aqui uso um {@code TransactionTemplate} explícito, na mão. */
	@AfterEach
	void cleanUp() {
		new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
			for (Long coupleId : createdCoupleIds) {
				coupleMembers.findByCoupleIdAndLeftAtIsNull(coupleId)
					.forEach(m -> coupleMembers.deleteById(m.getId()));
			}
			createdPairingIds.forEach(stories::deleteAllByPairingId);
			createdPairingIds.forEach(pairings::deleteById);
			createdCoupleIds.forEach(couples::deleteById);
			createdUserIds.forEach(users::deleteById);
		});
	}

	private UserSummary newUser() {
		String unique = UUID.randomUUID().toString();
		UserSummary user = userService.findOrCreateByGoogle("sub-" + unique, unique + "@example.com", "Pessoa Teste");
		createdUserIds.add(user.id());
		return user;
	}

	private UserSummary newUserWithHandle(String handle) {
		UserSummary user = newUser();
		return userService.chooseHandle(user.id(), handle);
	}

	private String bearerFor(Long userId) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("duo")
			.subject(userId.toString())
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(900))
			.build();
		String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();
		return "Bearer " + token;
	}

	private Long inviteByHandle(UserSummary initiator, String handle) throws Exception {
		MvcResult result = mvc
			.perform(post("/api/pairings/invite-by-handle").header("Authorization", bearerFor(initiator.id()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(new CouplePairingController.InviteByHandleRequest(handle))))
			.andReturn();
		Long pairingId = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
		createdPairingIds.add(pairingId);
		return pairingId;
	}

	@Test
	void requestsWithoutATokenAreRejected() throws Exception {
		mvc.perform(get("/api/pairings/mine")).andExpect(status().isUnauthorized());
	}

	@Test
	void theWholeBondingFlowWorksThroughTheRealControllers() throws Exception {
		String uniqueHandle = "handle_" + UUID.randomUUID().toString().substring(0, 8);
		UserSummary a = newUser();
		UserSummary b = newUserWithHandle(uniqueHandle);

		// A convida B pelo @handle.
		Long pairingId = inviteByHandle(a, uniqueHandle);

		// B vê o convite em "/mine".
		mvc.perform(get("/api/pairings/mine").header("Authorization", bearerFor(b.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(pairingId));

		// Antes de firmar, escrever a história é recusado (409) — a ordem é firmar primeiro.
		mvc
			.perform(put("/api/pairings/" + pairingId + "/story/HOW_WE_MET")
				.header("Authorization", bearerFor(a.id()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(new CouplePairingController.StoryStepRequest("Nos conhecemos."))))
			.andExpect(status().isConflict());

		// Os dois firmam.
		mvc.perform(post("/api/pairings/" + pairingId + "/confirm").header("Authorization", bearerFor(a.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.state").value("PENDING"));
		MvcResult confirmResult = mvc
			.perform(post("/api/pairings/" + pairingId + "/confirm").header("Authorization", bearerFor(b.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.state").value("CONFIRMED"))
			.andReturn();
		Long coupleId = json.readTree(confirmResult.getResponse().getContentAsString()).get("coupleId").asLong();
		createdCoupleIds.add(coupleId);

		// Agora sim a história pode ser escrita.
		mvc
			.perform(put("/api/pairings/" + pairingId + "/story/HOW_WE_MET")
				.header("Authorization", bearerFor(a.id()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(new CouplePairingController.StoryStepRequest("Nos conhecemos."))))
			.andExpect(status().isNoContent());

		// E o casal já aparece em "/couples/mine" para os dois.
		mvc.perform(get("/api/couples/mine").header("Authorization", bearerFor(a.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.state").value("ACTIVE"));
		mvc.perform(get("/api/couples/mine").header("Authorization", bearerFor(b.id())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.state").value("ACTIVE"));
	}

	@Test
	void invitingAnUnknownHandleIsAConflictNotACrash() throws Exception {
		UserSummary a = newUser();

		mvc
			.perform(post("/api/pairings/invite-by-handle").header("Authorization", bearerFor(a.id()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(
						new CouplePairingController.InviteByHandleRequest("ninguem_tem_esse_handle"))))
			.andExpect(status().isConflict());
	}

	@Test
	void someoneOutsideThePairingGetsA404NotTheRealReason() throws Exception {
		String uniqueHandle = "handle_" + UUID.randomUUID().toString().substring(0, 8);
		UserSummary a = newUser();
		UserSummary b = newUserWithHandle(uniqueHandle);
		UserSummary outsider = newUser();
		Long pairingId = inviteByHandle(a, uniqueHandle);

		// Controle positivo: B (que de fato participa) enxerga o mesmo pareamento sem problema.
		mvc.perform(get("/api/pairings/" + pairingId).header("Authorization", bearerFor(b.id())))
			.andExpect(status().isOk());

		mvc.perform(get("/api/pairings/" + pairingId).header("Authorization", bearerFor(outsider.id())))
			.andExpect(status().isNotFound());
		mvc.perform(post("/api/pairings/" + pairingId + "/confirm").header("Authorization", bearerFor(outsider.id())))
			.andExpect(status().isNotFound());
	}

	@Test
	void someoneOutsideACoupleGetsA404() throws Exception {
		String uniqueHandle = "handle_" + UUID.randomUUID().toString().substring(0, 8);
		UserSummary a = newUser();
		UserSummary b = newUserWithHandle(uniqueHandle);
		UserSummary outsider = newUser();
		Long pairingId = inviteByHandle(a, uniqueHandle);
		mvc.perform(post("/api/pairings/" + pairingId + "/confirm").header("Authorization", bearerFor(a.id())));
		MvcResult confirmResult = mvc
			.perform(post("/api/pairings/" + pairingId + "/confirm").header("Authorization", bearerFor(b.id())))
			.andReturn();
		Long coupleId = json.readTree(confirmResult.getResponse().getContentAsString()).get("coupleId").asLong();
		createdCoupleIds.add(coupleId);
		assertThat(coupleId).isNotNull();

		mvc.perform(get("/api/couples/" + coupleId).header("Authorization", bearerFor(outsider.id())))
			.andExpect(status().isNotFound());
		mvc.perform(post("/api/couples/" + coupleId + "/pause").header("Authorization", bearerFor(outsider.id())))
			.andExpect(status().isNotFound());
	}
}
