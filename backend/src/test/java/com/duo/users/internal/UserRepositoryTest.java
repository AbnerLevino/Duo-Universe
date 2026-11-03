package com.duo.users.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
		userRepository.save(new User("ana@example.com", "Ana"));

		assertThat(userRepository.findByEmailIgnoreCase("ANA@example.com")).isPresent();
	}

	@Test
	void fillsCreatedAtAutomatically() {
		User saved = userRepository.save(new User("bruno@example.com", "Bruno"));

		assertThat(saved.getCreatedAt()).isNotNull();
	}
}
