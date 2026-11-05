package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;

import com.duo.relationships.Channel;
import com.duo.relationships.IllegalPairingOperationException;
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

	/** As duas pessoas já confirmaram (RN-08) — estado em que história e datas passam a ser permitidas. */
	private CouplePairing confirmed() {
		CouplePairing pairing = pending();
		pairing.recordConfirmation(1L, NOW);
		pairing.recordConfirmation(2L, NOW);
		pairing.markConfirmed(NOW, 42L);
		return pairing;
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
	void recordConfirmationIsNotEnoughAloneToBeFullyConfirmed() {
		CouplePairing pairing = pending();

		pairing.recordConfirmation(1L, NOW);

		assertThat(pairing.getInitiatorConfirmedAt()).isEqualTo(NOW);
		assertThat(pairing.getInviteeConfirmedAt()).isNull();
		assertThat(pairing.isFullyConfirmed()).isFalse();
	}

	@Test
	void isFullyConfirmedOnlyOnceBothSidesRecorded() {
		CouplePairing pairing = pending();

		pairing.recordConfirmation(2L, NOW);
		assertThat(pairing.isFullyConfirmed()).isFalse();

		pairing.recordConfirmation(1L, NOW);
		assertThat(pairing.isFullyConfirmed()).isTrue();
	}

	@Test
	void recordConfirmationRejectsSomeoneOutsideThePairing() {
		CouplePairing pairing = pending();

		assertThatThrownBy(() -> pairing.recordConfirmation(99L, NOW)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void cannotRecordConfirmationOnceThePairingIsDecided() {
		CouplePairing pairing = pending();
		pairing.cancel(NOW);

		assertThatThrownBy(() -> pairing.recordConfirmation(1L, NOW))
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

	@Test
	void partnerOfReturnsTheOtherPerson() {
		CouplePairing pairing = pending();

		assertThat(pairing.partnerOf(1L)).isEqualTo(2L);
		assertThat(pairing.partnerOf(2L)).isEqualTo(1L);
	}

	@Test
	void partnerOfIsNullWhenThereIsNoInviteeYet() {
		CouplePairing pairing = new CouplePairing(1L, null, Channel.LINK, NOW.plusSeconds(60));

		assertThat(pairing.partnerOf(1L)).isNull();
	}

	@Test
	void partnerOfRejectsSomeoneOutsideThePairing() {
		CouplePairing pairing = pending();

		assertThatThrownBy(() -> pairing.partnerOf(99L)).isInstanceOf(IllegalArgumentException.class);
	}

	private static final LocalDate KNOWN_SINCE = LocalDate.of(2023, 5, 1);
	private static final LocalDate DATING_SINCE = LocalDate.of(2023, 8, 12);

	@Test
	void proposeDatesRequiresThePairingToBeConfirmedFirst() {
		CouplePairing pairing = pending();

		assertThatThrownBy(() -> pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE))
				.isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void acceptDatesAlsoRequiresThePairingToBeConfirmedFirst() {
		CouplePairing pairing = pending();

		assertThatThrownBy(() -> pairing.acceptDates(2L, NOW)).isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void proposeDatesRecordsTheProposerAndTheDates() {
		CouplePairing pairing = confirmed();

		pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE);

		assertThat(pairing.getDateProposedByUserId()).isEqualTo(1L);
		assertThat(pairing.getProposedKnownSinceDate()).isEqualTo(KNOWN_SINCE);
		assertThat(pairing.getProposedDatingSinceDate()).isEqualTo(DATING_SINCE);
		assertThat(pairing.areDatesAccepted()).isFalse();
	}

	@Test
	void acceptDatesRequiresAPriorProposal() {
		CouplePairing pairing = confirmed();

		assertThatThrownBy(() -> pairing.acceptDates(2L, NOW)).isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void theProposerCannotAcceptTheirOwnProposal() {
		CouplePairing pairing = confirmed();
		pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE);

		assertThatThrownBy(() -> pairing.acceptDates(1L, NOW)).isInstanceOf(IllegalPairingOperationException.class);
	}

	@Test
	void theOtherPersonAcceptingMarksTheDatesAsAccepted() {
		CouplePairing pairing = confirmed();
		pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE);

		pairing.acceptDates(2L, NOW);

		assertThat(pairing.areDatesAccepted()).isTrue();
		assertThat(pairing.getDatesAcceptedAt()).isEqualTo(NOW);
	}

	@Test
	void aNewProposalReopensConsensusAndDiscardsAPreviousAcceptance() {
		CouplePairing pairing = confirmed();
		pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE);
		pairing.acceptDates(2L, NOW);

		pairing.proposeDates(2L, KNOWN_SINCE.plusDays(1), DATING_SINCE);

		assertThat(pairing.areDatesAccepted()).isFalse();
		assertThat(pairing.getDateProposedByUserId()).isEqualTo(2L);
	}

	@Test
	void cannotProposeOrAcceptDatesOnceThePairingIsDecided() {
		CouplePairing pairing = pending();
		pairing.cancel(NOW);

		assertThatThrownBy(() -> pairing.proposeDates(1L, KNOWN_SINCE, DATING_SINCE))
				.isInstanceOf(IllegalPairingOperationException.class);
		assertThatThrownBy(() -> pairing.acceptDates(2L, NOW)).isInstanceOf(IllegalPairingOperationException.class);
	}

	// Não existe mais um teste "cancelar apaga a proposta de datas": pela ordem nova (firmar primeiro),
	// propor datas exige CONFIRMED e cancelar exige PENDING — os dois nunca acontecem com o mesmo pareamento,
	// então esse cenário não é mais alcançável pela API pública (ver o comentário em CouplePairing.cancel).

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
