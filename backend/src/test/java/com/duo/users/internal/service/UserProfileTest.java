package com.duo.users.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import com.duo.shared.ClockConfig;
import com.duo.users.MaritalStatus;
import com.duo.users.ProfileVisibility;
import com.duo.users.dto.ProfilePage;
import com.duo.users.dto.ProfileUpdate;
import com.duo.users.dto.UserProfile;
import com.duo.users.dto.UserSummary;
import com.duo.users.exception.HandleChangeLimitException;
import com.duo.users.exception.InvalidProfileException;
import com.duo.users.exception.InvalidSearchException;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;

/**
 * Épico 3, Etapas 2 e 4: o perfil (bio, profissão, estado civil, visibilidade), o limite de 2 trocas do DuoName
 * por dia e a busca por nome ou DuoName — tudo contra o {@code duo_test} de verdade.
 */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ UserServiceImpl.class, ClockConfig.class })
class UserProfileTest {

	@Autowired
	UserServiceImpl userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EntityManager entityManager;

	private UserSummary newUser(String key, String name) {
		return userService.findOrCreateByGoogle("sub-" + key, key + "@example.com", name);
	}

	private static ProfileUpdate update(String name, String bio, String profession) {
		return new ProfileUpdate(name, bio, profession, MaritalStatus.SINGLE, ProfileVisibility.PUBLIC);
	}

	// ---------- perfil (Etapa 2) ----------

	@Test
	void newAccountsStartSingleAndPublic() {
		UserSummary user = newUser("novo", "Novo");

		UserProfile profile = userService.findProfileById(user.id()).orElseThrow();

		assertThat(profile.maritalStatus()).isEqualTo(MaritalStatus.SINGLE);
		assertThat(profile.profileVisibility()).isEqualTo(ProfileVisibility.PUBLIC);
		assertThat(profile.bio()).isNull();
		assertThat(profile.hidden()).isFalse();
	}

	@Test
	void updatesAllProfileFieldsAndTrimsText() {
		UserSummary user = newUser("edita", "Edita");

		UserProfile saved = userService.updateProfile(user.id(), new ProfileUpdate("  Maria Souza ", " Gosto de café ",
				"Designer", MaritalStatus.DATING, ProfileVisibility.PRIVATE));

		assertThat(saved.displayName()).isEqualTo("Maria Souza");
		assertThat(saved.bio()).isEqualTo("Gosto de café");
		assertThat(saved.profession()).isEqualTo("Designer");
		assertThat(saved.maritalStatus()).isEqualTo(MaritalStatus.DATING);
		assertThat(saved.profileVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
	}

	@Test
	void blankBioAndProfessionBecomeNull() {
		UserSummary user = newUser("vazio", "Vazio");

		UserProfile saved = userService.updateProfile(user.id(), update("Vazio", "   ", ""));

		assertThat(saved.bio()).isNull();
		assertThat(saved.profession()).isNull();
	}

	@Test
	void rejectsFieldsOutsideTheRules() {
		UserSummary user = newUser("regras", "Regras");

		assertThatThrownBy(() -> userService.updateProfile(user.id(), update("A", null, null)))
			.isInstanceOf(InvalidProfileException.class);
		assertThatThrownBy(() -> userService.updateProfile(user.id(), update("x".repeat(51), null, null)))
			.isInstanceOf(InvalidProfileException.class);
		assertThatThrownBy(() -> userService.updateProfile(user.id(), update("Regras", "b".repeat(101), null)))
			.isInstanceOf(InvalidProfileException.class);
		assertThatThrownBy(() -> userService.updateProfile(user.id(), update("Regras", null, "p".repeat(21))))
			.isInstanceOf(InvalidProfileException.class);
		assertThatThrownBy(() -> userService.updateProfile(user.id(),
				new ProfileUpdate("Regras", null, null, null, ProfileVisibility.PUBLIC)))
			.isInstanceOf(InvalidProfileException.class);
	}

	@Test
	void acceptsBioOfExactlyTheLimits() {
		UserSummary user = newUser("limite", "Limite");

		UserProfile saved = userService.updateProfile(user.id(), update("Li", "b".repeat(100), "p".repeat(20)));

		assertThat(saved.bio()).hasSize(100);
		assertThat(saved.profession()).hasSize(20);
	}

	// ---------- limite de troca do DuoName ----------

	@Test
	void firstHandleChoiceDoesNotCountAndTwoChangesPerDayAreAllowed() {
		UserSummary user = newUser("handle1", "Handle Um");

		userService.chooseHandle(user.id(), "primeiro");
		userService.chooseHandle(user.id(), "segundo");
		UserSummary third = userService.chooseHandle(user.id(), "terceiro");

		assertThat(third.handle()).isEqualTo("terceiro");
		assertThatThrownBy(() -> userService.chooseHandle(user.id(), "quarto"))
			.isInstanceOf(HandleChangeLimitException.class);
	}

	@Test
	void changingOnlyTheLetterCaseDoesNotUseUpAChange() {
		UserSummary user = newUser("handle2", "Handle Dois");
		userService.chooseHandle(user.id(), "fulano");

		userService.chooseHandle(user.id(), "Fulano");
		userService.chooseHandle(user.id(), "FULANO");
		userService.chooseHandle(user.id(), "ciclano");
		UserSummary last = userService.chooseHandle(user.id(), "beltrano");

		assertThat(last.handle()).isEqualTo("beltrano");
	}

	@Test
	void theLimitResetsTheNextDay() {
		UserSummary user = newUser("handle3", "Handle Tres");
		Instant today = Instant.parse("2026-11-06T12:00:00Z");
		UserServiceImpl todayService = new UserServiceImpl(userRepository, Clock.fixed(today, ZoneOffset.UTC));
		todayService.chooseHandle(user.id(), "um_dia");
		todayService.chooseHandle(user.id(), "dois_dia");
		todayService.chooseHandle(user.id(), "tres_dia");
		assertThatThrownBy(() -> todayService.chooseHandle(user.id(), "quatro_dia"))
			.isInstanceOf(HandleChangeLimitException.class);

		UserServiceImpl tomorrowService = new UserServiceImpl(userRepository,
				Clock.fixed(today.plusSeconds(24 * 3600), ZoneOffset.UTC));

		assertThat(tomorrowService.chooseHandle(user.id(), "quatro_dia").handle()).isEqualTo("quatro_dia");
	}

	// ---------- busca (Etapa 4) ----------

	@Test
	void searchIgnoresAccentsAndCase() {
		UserSummary joao = newUser("busca1", "João da Silva");

		ProfilePage page = userService.searchProfiles("JOAO", List.of(), 0, 20);

		assertThat(page.items()).extracting(UserProfile::id).contains(joao.id());
	}

	@Test
	void searchFindsByDuoNamePrefixAndPutsTheExactDuoNameFirst() {
		UserSummary exact = newUser("busca2", "Zeca Exato");
		userService.chooseHandle(exact.id(), "maria_x");
		UserSummary prefix = newUser("busca3", "Alice Prefixo");
		userService.chooseHandle(prefix.id(), "maria_xyz");

		ProfilePage page = userService.searchProfiles("maria_x", List.of(), 0, 20);

		assertThat(page.items()).extracting(UserProfile::id).containsExactly(exact.id(), prefix.id());
	}

	@Test
	void searchTermNeedsAtLeastThreeLetters() {
		assertThatThrownBy(() -> userService.searchProfiles("ab", List.of(), 0, 20))
			.isInstanceOf(InvalidSearchException.class);
		assertThatThrownBy(() -> userService.searchProfiles("  a ", List.of(), 0, 20))
			.isInstanceOf(InvalidSearchException.class);
	}

	@Test
	void likeWildcardsInTheTermAreTreatedAsPlainText() {
		newUser("busca4", "Pessoa Comum");

		// "%%%" viraria "tudo" se o curinga do LIKE não fosse escapado
		assertThat(userService.searchProfiles("%%%", List.of(), 0, 20).items()).isEmpty();
		assertThat(userService.searchProfiles("pe_s", List.of(), 0, 20).items()).isEmpty();
	}

	@Test
	void searchSkipsExcludedAndHiddenAccounts() {
		UserSummary visible = newUser("busca5", "Carlota Visivel");
		UserSummary excluded = newUser("busca6", "Carlota Excluida");
		UserSummary hidden = newUser("busca7", "Carlota Oculta");
		entityManager.createNativeQuery("update users set hidden_at = now() where id = :id")
			.setParameter("id", hidden.id())
			.executeUpdate();
		entityManager.clear();

		ProfilePage page = userService.searchProfiles("carlota", List.of(excluded.id()), 0, 20);

		assertThat(page.items()).extracting(UserProfile::id).contains(visible.id()).doesNotContain(excluded.id(),
				hidden.id());
	}

	@Test
	void searchPagesTellIfThereIsAnotherPage() {
		for (int i = 0; i < 3; i++) {
			newUser("pagina" + i, "Paginado " + i);
		}

		ProfilePage first = userService.searchProfiles("paginado", List.of(), 0, 2);
		ProfilePage second = userService.searchProfiles("paginado", List.of(), 1, 2);

		assertThat(first.items()).hasSize(2);
		assertThat(first.hasMore()).isTrue();
		assertThat(second.items()).hasSize(1);
		assertThat(second.hasMore()).isFalse();
	}
}
