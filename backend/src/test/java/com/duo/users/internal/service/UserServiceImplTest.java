package com.duo.users.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.duo.users.dto.UserSummary;
import com.duo.users.exception.HandleTakenException;
import com.duo.users.exception.InvalidHandleException;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserServiceImpl.class)
class UserServiceImplTest {

	@Autowired
	UserServiceImpl userService;

	@Autowired
	UserRepository userRepository;

	@Test
	void createsAccountOnFirstGoogleLogin() {
		UserSummary created = userService.findOrCreateByGoogle("sub-eva", "eva@example.com", "Eva");

		assertThat(created.id()).isNotNull();
		User saved = userRepository.findByGoogleSub("sub-eva").orElseThrow();
		assertThat(saved.getEmailVerifiedAt()).isNotNull();
		assertThat(saved.getDisplayName()).isEqualTo("Eva");
	}

	@Test
	void reusesAccountAndKeepsEditedNameOnNextLogin() {
		UserSummary first = userService.findOrCreateByGoogle("sub-fabio", "fabio@example.com", "Fabio");
		userRepository.findByGoogleSub("sub-fabio").orElseThrow().setDisplayName("Fabinho");

		UserSummary second = userService.findOrCreateByGoogle("sub-fabio", "fabio@example.com", "Fabio");

		assertThat(second.id()).isEqualTo(first.id());
		assertThat(second.displayName()).isEqualTo("Fabinho");
	}

	@Test
	void fallsBackToEmailNameWhenGoogleSendsNoName() {
		UserSummary created = userService.findOrCreateByGoogle("sub-gui", "gui@example.com", null);

		assertThat(created.displayName()).isEqualTo("gui");
	}

	@Test
	void choosesHandleAndFindsAccountByItIgnoringCase() {
		UserSummary user = userService.findOrCreateByGoogle("sub-helo", "helo@example.com", "Helo");

		userService.chooseHandle(user.id(), "Helo_123");

		assertThat(userService.findByHandle("helo_123").orElseThrow().id()).isEqualTo(user.id());
	}

	@Test
	void rejectsHandleAlreadyTakenByAnotherAccountIgnoringCase() {
		UserSummary first = userService.findOrCreateByGoogle("sub-ines", "ines@example.com", "Ines");
		UserSummary second = userService.findOrCreateByGoogle("sub-joao", "joao@example.com", "Joao");
		userService.chooseHandle(first.id(), "mesmo_handle");

		assertThatThrownBy(() -> userService.chooseHandle(second.id(), "Mesmo_Handle"))
			.isInstanceOf(HandleTakenException.class);
	}

	@Test
	void rejectsHandleWithInvalidFormat() {
		UserSummary user = userService.findOrCreateByGoogle("sub-kat", "kat@example.com", "Kat");

		assertThatThrownBy(() -> userService.chooseHandle(user.id(), "ok"))
			.isInstanceOf(InvalidHandleException.class);
		assertThatThrownBy(() -> userService.chooseHandle(user.id(), "tem espaço"))
			.isInstanceOf(InvalidHandleException.class);
	}
}
