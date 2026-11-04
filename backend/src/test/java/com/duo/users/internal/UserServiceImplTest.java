package com.duo.users.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.duo.users.UserSummary;

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
}
