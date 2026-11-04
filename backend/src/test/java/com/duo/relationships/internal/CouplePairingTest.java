package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.duo.relationships.Channel;
import com.duo.relationships.IllegalPairingTransitionException;
import com.duo.relationships.InviteCodeInvalidException;
import com.duo.relationships.PairingState;

import org.junit.jupiter.api.Test;

/** A máquina de estados pura do convite (RN-08), sem banco envolvido. */
class CouplePairingTest {

	private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

	private CouplePairing pending() {
		return new CouplePairing(1L, 2L, Channel.HANDLE, NOW.plusSeconds(60));
	}

	@Test
	void startsAsPending() {
		assertThat(pending().getState()).isEqualTo(PairingState.PENDING);
	}

	@Test
	void cancelMovesToCancelled() {
		CouplePairing pairing = pending();

		pairing.cancel(NOW);

		assertThat(pairing.getState()).isEqualTo(PairingState.CANCELLED);
		assertThat(pairing.getDecidedAt()).isEqualTo(NOW);
	}

	@Test
	void markConfirmedMovesToConfirmedAndRecordsTheCouple() {
		CouplePairing pairing = pending();

		pairing.markConfirmed(NOW, 42L);

		assertThat(pairing.getState()).isEqualTo(PairingState.CONFIRMED);
		assertThat(pairing.getCoupleId()).isEqualTo(42L);
	}

	@Test
	void cancellingTwiceIsAnIllegalTransition() {
		CouplePairing pairing = pending();
		pairing.cancel(NOW);

		assertThatThrownBy(() -> pairing.cancel(NOW)).isInstanceOf(IllegalPairingTransitionException.class);
	}

	@Test
	void cannotConfirmAPairingThatWasAlreadyCancelled() {
		CouplePairing pairing = pending();
		pairing.cancel(NOW);

		assertThatThrownBy(() -> pairing.markConfirmed(NOW, 42L))
				.isInstanceOf(IllegalPairingTransitionException.class);
	}

	@Test
	void isExpiredOnlyWhilePendingAndPastTheDeadline() {
		CouplePairing pairing = new CouplePairing(1L, 2L, Channel.LINK, NOW);

		assertThat(pairing.isExpired(NOW.minusSeconds(1))).isFalse();
		assertThat(pairing.isExpired(NOW)).isTrue();

		pairing.cancel(NOW);
		assertThat(pairing.isExpired(NOW.plusSeconds(60))).isFalse();
	}

	@Test
	void involvesOnlyInitiatorAndInvitee() {
		CouplePairing pairing = pending();

		assertThat(pairing.involves(1L)).isTrue();
		assertThat(pairing.involves(2L)).isTrue();
		assertThat(pairing.involves(99L)).isFalse();
	}

	@Test
	void involvesOnlyTheInitiatorWhenThereIsNoInviteeYet() {
		CouplePairing pairing = new CouplePairing(1L, null, Channel.LINK, NOW.plusSeconds(60));

		assertThat(pairing.involves(1L)).isTrue();
		assertThat(pairing.involves(2L)).isFalse();
	}

	private CouplePairing linkPairingWithoutInvitee() {
		return new CouplePairing(1L, null, Channel.LINK, NOW.plusSeconds(60), "hash-do-codigo");
	}

	@Test
	void redeemLinkFillsInTheInvitee() {
		CouplePairing pairing = linkPairingWithoutInvitee();

		pairing.redeemLink(2L, NOW);

		assertThat(pairing.getInviteeUserId()).isEqualTo(2L);
		assertThat(pairing.involves(2L)).isTrue();
	}

	@Test
	void redeemLinkRejectsASecondUse() {
		CouplePairing pairing = linkPairingWithoutInvitee();
		pairing.redeemLink(2L, NOW);

		assertThatThrownBy(() -> pairing.redeemLink(3L, NOW)).isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemLinkRejectsAnExpiredCode() {
		CouplePairing pairing = linkPairingWithoutInvitee();

		assertThatThrownBy(() -> pairing.redeemLink(2L, NOW.plusSeconds(61)))
				.isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemLinkRejectsTheInitiatorUsingTheirOwnCode() {
		CouplePairing pairing = linkPairingWithoutInvitee();

		assertThatThrownBy(() -> pairing.redeemLink(1L, NOW)).isInstanceOf(InviteCodeInvalidException.class);
	}

	@Test
	void redeemLinkRejectsACancelledPairing() {
		CouplePairing pairing = linkPairingWithoutInvitee();
		pairing.cancel(NOW);

		assertThatThrownBy(() -> pairing.redeemLink(2L, NOW)).isInstanceOf(InviteCodeInvalidException.class);
	}
}
