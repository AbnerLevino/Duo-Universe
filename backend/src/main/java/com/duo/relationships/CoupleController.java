package com.duo.relationships;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposição HTTP do Couple Universe já nascido (Etapa 13): pausar, retomar, sair (RN-13 a 18). */
@RestController
@RequestMapping("/api/couples")
class CoupleController {

	private final CoupleService couples;

	CoupleController(CoupleService couples) {
		this.couples = couples;
	}

	/** O que o app consulta ao abrir: o casal ativo da pessoa logada, se tiver. */
	@GetMapping("/mine")
	ResponseEntity<CoupleSummary> mine(@AuthenticationPrincipal Jwt jwt) {
		Optional<CoupleSummary> couple = couples.findActiveForUser(userId(jwt));
		return couple.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/{id}")
	CoupleSummary find(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return couples.find(id, userId(jwt));
	}

	@PostMapping("/{id}/pause")
	CoupleSummary pause(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return couples.pause(id, userId(jwt));
	}

	@PostMapping("/{id}/resume")
	CoupleSummary resume(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return couples.resume(id, userId(jwt));
	}

	@PostMapping("/{id}/leave")
	ResponseEntity<Void> leave(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		couples.leave(id, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
