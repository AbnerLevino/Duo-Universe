package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.duo.relationships.CoupleState;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CoupleMemberRepositoryTest {

	@Autowired
	CoupleRepository couples;

	@Autowired
	CoupleMemberRepository members;

	@Autowired
	UserRepository users;

	private Long newUserId(String suffix) {
		return users.save(new User("sub-cm-" + suffix, "cm-" + suffix + "@example.com", "CM " + suffix)).getId();
	}

	@Test
	void savesAndFindsTheActiveMembershipOfAPerson() {
		Long userId = newUserId("a");
		Couple couple = couples.save(new Couple(CoupleState.ACTIVE));

		members.save(new CoupleMember(couple.getId(), userId));

		assertThat(members.findByUserIdAndLeftAtIsNull(userId)).isPresent();
	}

	@Test
	void rejectsASecondActiveMembershipForTheSamePerson() {
		Long userId = newUserId("b");
		Couple first = couples.save(new Couple(CoupleState.ACTIVE));
		Couple second = couples.save(new Couple(CoupleState.ACTIVE));
		members.saveAndFlush(new CoupleMember(first.getId(), userId));

		assertThatThrownBy(() -> members.saveAndFlush(new CoupleMember(second.getId(), userId)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void allowsANewActiveMembershipAfterLeavingTheFirstOne() {
		Long userId = newUserId("c");
		Couple first = couples.save(new Couple(CoupleState.ENDED));
		CoupleMember membership = members.saveAndFlush(new CoupleMember(first.getId(), userId));
		membership.leave(Instant.now());
		members.saveAndFlush(membership);

		Couple second = couples.save(new Couple(CoupleState.ACTIVE));
		members.saveAndFlush(new CoupleMember(second.getId(), userId));

		assertThat(members.findByUserIdAndLeftAtIsNull(userId)).isPresent();
	}
}
