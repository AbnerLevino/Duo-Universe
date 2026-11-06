package com.duo.relationships.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.duo.relationships.CoupleState;
import com.duo.relationships.exception.IllegalCoupleTransitionException;

import org.junit.jupiter.api.Test;

/** A máquina de estados pura do Couple (RN-13 a RN-18), sem banco envolvido. */
class CoupleTest {

	private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

	@Test
	void pauseMovesFromActiveToPaused() {
		Couple couple = new Couple(CoupleState.ACTIVE);

		couple.pause(NOW);

		assertThat(couple.getState()).isEqualTo(CoupleState.PAUSED);
		assertThat(couple.getPausedAt()).isEqualTo(NOW);
	}

	@Test
	void cannotPauseACoupleThatIsNotActive() {
		Couple couple = new Couple(CoupleState.PAUSED);

		assertThatThrownBy(() -> couple.pause(NOW)).isInstanceOf(IllegalCoupleTransitionException.class);
	}

	@Test
	void resumeMovesFromPausedToActiveAndClearsPausedAt() {
		Couple couple = new Couple(CoupleState.ACTIVE);
		couple.pause(NOW);

		couple.resume(NOW.plusSeconds(60));

		assertThat(couple.getState()).isEqualTo(CoupleState.ACTIVE);
		assertThat(couple.getPausedAt()).isNull();
	}

	@Test
	void cannotResumeACoupleThatIsNotPaused() {
		Couple couple = new Couple(CoupleState.ACTIVE);

		assertThatThrownBy(() -> couple.resume(NOW)).isInstanceOf(IllegalCoupleTransitionException.class);
	}

	@Test
	void endWorksFromActiveOrPaused() {
		Couple fromActive = new Couple(CoupleState.ACTIVE);
		Couple fromPaused = new Couple(CoupleState.ACTIVE);
		fromPaused.pause(NOW);

		fromActive.end(NOW);
		fromPaused.end(NOW);

		assertThat(fromActive.getState()).isEqualTo(CoupleState.ENDED);
		assertThat(fromPaused.getState()).isEqualTo(CoupleState.ENDED);
		assertThat(fromActive.getEndedAt()).isEqualTo(NOW);
	}

	@Test
	void endIsFinalAndCannotHappenTwice() {
		Couple couple = new Couple(CoupleState.ACTIVE);
		couple.end(NOW);

		assertThatThrownBy(() -> couple.end(NOW)).isInstanceOf(IllegalCoupleTransitionException.class);
	}

	@Test
	void cannotPauseOrResumeAnEndedCouple() {
		Couple couple = new Couple(CoupleState.ACTIVE);
		couple.end(NOW);

		assertThatThrownBy(() -> couple.pause(NOW)).isInstanceOf(IllegalCoupleTransitionException.class);
		assertThatThrownBy(() -> couple.resume(NOW)).isInstanceOf(IllegalCoupleTransitionException.class);
	}
}
