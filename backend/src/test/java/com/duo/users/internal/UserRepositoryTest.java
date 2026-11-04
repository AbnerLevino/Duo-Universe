package com.duo.users.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Test
	void savesAndFindsByEmailIgnoringCase() {
		userRepository.save(new User("sub-ana", "ana@example.com", "Ana"));

		assertThat(userRepository.findByEmailIgnoreCase("ANA@example.com")).isPresent();
	}

	@Test
	void fillsCreatedAtAutomatically() {
		User saved = userRepository.save(new User("sub-bruno", "bruno@example.com", "Bruno"));

		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void findsByGoogleSub() {
		userRepository.save(new User("sub-carla", "carla@example.com", "Carla"));

		assertThat(userRepository.findByGoogleSub("sub-carla")).isPresent();
		assertThat(userRepository.findByGoogleSub("sub-inexistente")).isEmpty();
	}

	@Test
	void rejectsDuplicateGoogleSub() {
		userRepository.saveAndFlush(new User("sub-dani", "dani@example.com", "Dani"));

		assertThatThrownBy(() -> userRepository.saveAndFlush(new User("sub-dani", "outro@example.com", "Outra")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
