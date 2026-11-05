package com.duo.relationships;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposição HTTP do convite para o vínculo (Etapa 13). Só chama {@link CouplePairingService} — nunca toca
 * repositório direto. A pessoa autenticada vem sempre do token de acesso (RN-20), nunca de um id do pedido.
 */
@RestController
@RequestMapping("/api/pairings")
class CouplePairingController {

	private final CouplePairingService pairings;

	CouplePairingController(CouplePairingService pairings) {
		this.pairings = pairings;
	}

	record InviteByHandleRequest(String handle) {
	}

	record RedeemLinkRequest(String code) {
	}

	record StoryStepRequest(String text) {
	}

	record DatesRequest(LocalDate knownSinceDate, LocalDate datingSinceDate) {
	}

	@PostMapping("/invite-by-handle")
	PairingSummary inviteByHandle(@AuthenticationPrincipal Jwt jwt, @RequestBody InviteByHandleRequest request) {
		return pairings.inviteByHandle(userId(jwt), request.handle());
	}

	@PostMapping("/invite-by-link")
	IssuedPairingInvite inviteByLink(@AuthenticationPrincipal Jwt jwt) {
		return pairings.inviteByLink(userId(jwt));
	}

	@PostMapping("/redeem")
	PairingSummary redeem(@AuthenticationPrincipal Jwt jwt, @RequestBody RedeemLinkRequest request) {
		return pairings.redeemByLink(request.code(), userId(jwt));
	}

	/** O que o app consulta ao abrir: convites ainda em aberto da pessoa logada. */
	@GetMapping("/mine")
	List<PairingSummary> mine(@AuthenticationPrincipal Jwt jwt) {
		return pairings.findPendingForUser(userId(jwt));
	}

	@GetMapping("/{id}")
	PairingSummary find(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return pairings.find(id, userId(jwt));
	}

	@PostMapping("/{id}/confirm")
	PairingSummary confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return pairings.confirm(id, userId(jwt));
	}

	@PostMapping("/{id}/cancel")
	ResponseEntity<Void> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		pairings.cancel(id, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/story/{step}")
	ResponseEntity<Void> saveStoryStep(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@PathVariable StoryStep step, @RequestBody StoryStepRequest request) {
		pairings.saveStoryDraft(id, userId(jwt), step, request.text());
		return ResponseEntity.noContent().build();
	}

	/** RN-26: só devolve algo quando as duas histórias já estiverem completas. */
	@GetMapping("/{id}/partner-story")
	ResponseEntity<StorySummary> partnerStory(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		Optional<StorySummary> story = pairings.readPartnerStory(id, userId(jwt));
		return story.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PutMapping("/{id}/dates")
	ResponseEntity<Void> proposeDates(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@RequestBody DatesRequest request) {
		pairings.proposeDates(id, userId(jwt), request.knownSinceDate(), request.datingSinceDate());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/dates/accept")
	ResponseEntity<Void> acceptDates(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		pairings.acceptDates(id, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
