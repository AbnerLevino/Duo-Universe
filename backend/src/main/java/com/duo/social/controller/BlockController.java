package com.duo.social.controller;

import java.util.List;

import com.duo.social.BlockService;
import com.duo.social.dto.ProfileCard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bloquear e desbloquear (silencioso: a outra pessoa não é avisada). */
@RestController
class BlockController {

	private final BlockService blocks;

	BlockController(BlockService blocks) {
		this.blocks = blocks;
	}

	@PutMapping("/api/me/blocks/{handle}")
	ResponseEntity<Void> block(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		blocks.block(Long.valueOf(jwt.getSubject()), handle);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/api/me/blocks/{handle}")
	ResponseEntity<Void> unblock(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		blocks.unblock(Long.valueOf(jwt.getSubject()), handle);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/me/blocks")
	List<ProfileCard> blocked(@AuthenticationPrincipal Jwt jwt) {
		return blocks.blocked(Long.valueOf(jwt.getSubject()));
	}
}
