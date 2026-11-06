package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.duo.relationships.AlreadyInRelationshipException;
import com.duo.relationships.Channel;
import com.duo.relationships.CouplePairingService;
import com.duo.relationships.CoupleState;
import com.duo.relationships.PairingState;
import com.duo.relationships.PairingSummary;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * RN-06/RN-08 sob concorrência de verdade — duas threads, duas transações de verdade, não só duas chamadas
 * em sequência.
 *
 * <p>Por isso este teste usa {@code @SpringBootTest} (sobe o contexto inteiro do Spring) em vez do
 * {@code @DataJpaTest} que o resto do módulo usa. O {@code @DataJpaTest} monta só um "fatia" do contexto
 * (bancos e repositórios) — por isso os outros testes montam o {@code CouplePairingServiceImpl} na mão, com
 * {@code new}. Isso funciona para testar lógica, mas um objeto criado com {@code new} nunca passa pelo proxy
 * que o Spring usa para aplicar {@code @Transactional}/{@code @Lock} — fora do Spring, essas anotações são
 * só texto, não fazem nada. Para testar a trava e a transação separada de verdade, preciso do bean de
 * verdade, gerenciado pelo Spring — só assim as anotações realmente entram em ação.
 *
 * <p>Sem transação de teste embrulhando tudo (diferente do {@code @DataJpaTest}), nada aqui desfaz sozinho:
 * cada teste limpa manualmente (por id) o que criou, no {@code @AfterEach}.
 */
@ActiveProfiles("test")
@SpringBootTest
class CouplePairingConcurrencyTest {

	@Autowired
	CouplePairingService service;

	@Autowired
	CouplePairingRepository pairings;

	@Autowired
	CoupleRepository couples;

	@Autowired
	CoupleMemberRepository coupleMembers;

	@Autowired
	UserRepository users;

	private final List<Long> createdUserIds = new ArrayList<>();
	private final List<Long> createdPairingIds = new ArrayList<>();
	private final List<Long> createdCoupleIds = new ArrayList<>();

	private Long newUser() {
		String unique = UUID.randomUUID().toString();
		Long id = users.save(new User("sub-cc-" + unique, "cc-" + unique + "@example.com", "Teste")).getId();
		createdUserIds.add(id);
		return id;
	}

	private PairingSummary initiate(Long initiatorId, Long inviteeId) {
		PairingSummary pairing = service.initiate(initiatorId, inviteeId, Channel.HANDLE);
		createdPairingIds.add(pairing.id());
		return pairing;
	}

	@AfterEach
	void cleanUp() {
		for (Long coupleId : createdCoupleIds) {
			coupleMembers.findByCoupleIdAndLeftAtIsNull(coupleId).forEach(m -> coupleMembers.deleteById(m.getId()));
		}
		createdPairingIds.forEach(pairings::deleteById);
		createdCoupleIds.forEach(couples::deleteById);
		createdUserIds.forEach(users::deleteById);
	}

	/**
	 * Corrida 1: os dois membros do mesmo pareamento confirmam "ao mesmo tempo" de verdade. Sem a trava
	 * ({@code findByIdForUpdate}, RN-08), as duas leituras aconteceriam antes de qualquer das duas escrever,
	 * e uma das confirmações se perderia por sobrescrita (um "lost update") — o Couple não nasceria, mesmo
	 * com as duas pessoas tendo confirmado de verdade.
	 */
	@Test
	void confirmingTheSamePairingFromBothSidesAtTheSameInstantNeverLosesAConfirmation() throws Exception {
		Long aId = newUser();
		Long bId = newUser();
		PairingSummary pairing = initiate(aId, bId);

		CountDownLatch readyLatch = new CountDownLatch(2);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Future<PairingSummary> resultA;
		Future<PairingSummary> resultB;
		try {
			resultA = pool.submit(confirmAfterLatch(readyLatch, pairing.id(), aId));
			resultB = pool.submit(confirmAfterLatch(readyLatch, pairing.id(), bId));
		}
		finally {
			pool.shutdown();
		}
		resultA.get(10, TimeUnit.SECONDS);
		resultB.get(10, TimeUnit.SECONDS);

		CouplePairing stored = pairings.findById(pairing.id()).orElseThrow();
		assertThat(stored.getInitiatorConfirmedAt()).as("confirmação de A não pode se perder").isNotNull();
		assertThat(stored.getInviteeConfirmedAt()).as("confirmação de B não pode se perder").isNotNull();
		assertThat(stored.getState()).isEqualTo(PairingState.CONFIRMED);

		Long coupleId = stored.getCoupleId();
		assertThat(coupleId).as("o Couple precisa ter nascido").isNotNull();
		createdCoupleIds.add(coupleId);
		// Nasceu só UM casal, e os dois ficaram nele — não dois casais diferentes por engano.
		assertThat(coupleMembers.findByUserIdAndLeftAtIsNull(aId).orElseThrow().getCoupleId()).isEqualTo(coupleId);
		assertThat(coupleMembers.findByUserIdAndLeftAtIsNull(bId).orElseThrow().getCoupleId()).isEqualTo(coupleId);
		assertThat(couples.findById(coupleId).orElseThrow().getState()).isEqualTo(CoupleState.ACTIVE);
	}

	private Callable<PairingSummary> confirmAfterLatch(CountDownLatch readyLatch, Long pairingId, Long userId) {
		return () -> {
			readyLatch.countDown();
			readyLatch.await();
			return service.confirm(pairingId, userId);
		};
	}

	/**
	 * Corrida 2: a mesma pessoa (A) confirma dois pareamentos diferentes (com B e com C) quase ao mesmo
	 * tempo — o último passo que falta em cada um. O índice único do banco (RN-06, Etapa 1) só deixa A
	 * entrar num dos dois; o {@code CoupleFactory} roda em transação própria (REQUIRES_NEW) para que, quando
	 * o segundo nascimento falhar, só ele desfaça — a confirmação da pessoa que "perdeu" continua gravada.
	 */
	@Test
	void confirmingTwoDifferentPairingsForTheSamePersonAtTheSameInstantOnlyBirthsOneCouple() throws Exception {
		Long aId = newUser();
		Long bId = newUser();
		Long cId = newUser();
		PairingSummary pairingWithB = initiate(aId, bId);
		PairingSummary pairingWithC = initiate(aId, cId);
		// A já confirmou os dois de antemão; só falta a outra ponta de cada um — é essa ponta que vai
		// disputar a corrida.
		service.confirm(pairingWithB.id(), aId);
		service.confirm(pairingWithC.id(), aId);

		CountDownLatch readyLatch = new CountDownLatch(2);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Future<Object> resultB;
		Future<Object> resultC;
		try {
			resultB = pool.submit(confirmCatchingConflict(readyLatch, pairingWithB.id(), bId));
			resultC = pool.submit(confirmCatchingConflict(readyLatch, pairingWithC.id(), cId));
		}
		finally {
			pool.shutdown();
		}
		Object outcomeB = resultB.get(10, TimeUnit.SECONDS);
		Object outcomeC = resultC.get(10, TimeUnit.SECONDS);

		boolean bWon = outcomeB instanceof PairingSummary;
		boolean cWon = outcomeC instanceof PairingSummary;
		assertThat(bWon ^ cWon).as("exatamente um dos dois pareamentos deveria ter conseguido nascer").isTrue();
		assertThat(bWon ? outcomeC : outcomeB).isInstanceOf(AlreadyInRelationshipException.class);

		Long winningPairingId = bWon ? pairingWithB.id() : pairingWithC.id();
		Long losingPairingId = bWon ? pairingWithC.id() : pairingWithB.id();

		CouplePairing winning = pairings.findById(winningPairingId).orElseThrow();
		assertThat(winning.getState()).isEqualTo(PairingState.CONFIRMED);
		Long coupleId = winning.getCoupleId();
		assertThat(coupleId).isNotNull();
		createdCoupleIds.add(coupleId);

		// A confirmação de quem perdeu a corrida continua gravada — só o Couple é que não nasceu para ela.
		CouplePairing losing = pairings.findById(losingPairingId).orElseThrow();
		assertThat(losing.isFullyConfirmed()).as("a confirmação legítima não pode se perder").isTrue();
		assertThat(losing.getCoupleId()).isNull();
		assertThat(losing.getState()).isEqualTo(PairingState.PENDING);

		// RN-06: A continua em exatamente um vínculo ativo, nunca em dois.
		assertThat(coupleMembers.findByUserIdAndLeftAtIsNull(aId).orElseThrow().getCoupleId()).isEqualTo(coupleId);
	}

	private Callable<Object> confirmCatchingConflict(CountDownLatch readyLatch, Long pairingId, Long userId) {
		return () -> {
			readyLatch.countDown();
			readyLatch.await();
			try {
				return service.confirm(pairingId, userId);
			}
			catch (AlreadyInRelationshipException conflict) {
				return conflict;
			}
		};
	}
}
