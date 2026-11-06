package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.duo.relationships.CoupleNotFoundException;
import com.duo.relationships.CoupleState;
import com.duo.relationships.CoupleSummary;
import com.duo.relationships.IllegalCoupleTransitionException;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/** RN-13 a RN-18 contra o banco de verdade (mesmo padrão de CouplePairingServiceImplTest). */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CoupleServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

	@Autowired
	CoupleRepository couples;

	@Autowired
	CoupleMemberRepository members;

	@Autowired
	UserRepository users;

	Long aId;
	Long bId;
	Long coupleId;

	@BeforeEach
	void createActiveCouple() {
		aId = users.save(new User("sub-co-a", "co-a@example.com", "A")).getId();
		bId = users.save(new User("sub-co-b", "co-b@example.com", "B")).getId();
		Couple couple = couples.save(new Couple(CoupleState.ACTIVE));
		members.save(new CoupleMember(couple.getId(), aId));
		members.save(new CoupleMember(couple.getId(), bId));
		coupleId = couple.getId();
	}

	private CoupleServiceImpl serviceAt(Instant now) {
		return new CoupleServiceImpl(couples, members, Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void eitherMemberCanPauseAnActiveCouple() {
		CoupleSummary summary = serviceAt(NOW).pause(coupleId, bId);

		assertThat(summary.state()).isEqualTo(CoupleState.PAUSED);
		assertThat(summary.pausedAt()).isEqualTo(NOW);
	}

	@Test
	void eitherMemberCanResumeAPausedCouple() {
		CoupleServiceImpl service = serviceAt(NOW);
		service.pause(coupleId, aId);

		CoupleSummary summary = service.resume(coupleId, bId);

		assertThat(summary.state()).isEqualTo(CoupleState.ACTIVE);
	}

	@Test
	void someoneOutsideTheCoupleCannotSeeOrPauseIt() {
		CoupleServiceImpl service = serviceAt(NOW);
		Long outsider = users.save(new User("sub-co-c", "co-c@example.com", "C")).getId();

		assertThatThrownBy(() -> service.find(coupleId, outsider)).isInstanceOf(CoupleNotFoundException.class);
		assertThatThrownBy(() -> service.pause(coupleId, outsider)).isInstanceOf(CoupleNotFoundException.class);
	}

	@Test
	void leavingEndsTheWholeCoupleForBothMembersInTheMvp() {
		CoupleServiceImpl service = serviceAt(NOW);

		service.leave(coupleId, aId);

		assertThat(couples.findById(coupleId).orElseThrow().getState()).isEqualTo(CoupleState.ENDED);
		assertThat(members.findByCoupleIdAndLeftAtIsNull(coupleId)).isEmpty();
	}

	@Test
	void afterLeavingNeitherFormerMemberCanSeeTheCoupleAnymore() {
		CoupleServiceImpl service = serviceAt(NOW);
		service.leave(coupleId, aId);

		assertThatThrownBy(() -> service.find(coupleId, aId)).isInstanceOf(CoupleNotFoundException.class);
		assertThatThrownBy(() -> service.find(coupleId, bId)).isInstanceOf(CoupleNotFoundException.class);
	}

	@Test
	void cannotPauseOrResumeAnEndedCouple() {
		CoupleServiceImpl service = serviceAt(NOW);
		service.leave(coupleId, aId);
		members.flush(); // RN-06: sem isso, o índice único ainda veria a filiação antiga como ativa.
		Couple ended = couples.save(new Couple(CoupleState.ENDED));
		members.save(new CoupleMember(ended.getId(), aId));

		assertThatThrownBy(() -> service.pause(ended.getId(), aId))
				.isInstanceOf(IllegalCoupleTransitionException.class);
	}
}
