package com.duo.social.controller;

import java.util.List;

import com.duo.social.FollowService;
import com.duo.social.SocialFeature;
import com.duo.social.dto.ProfileCard;
import com.duo.social.dto.Relation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Duollowers e Duollowing: seguir, pedidos, aceitar, recusar e remover. */
@RestController
@ConditionalOnProperty(name = SocialFeature.PROPERTY, havingValue = "true")
class FollowController {

	private final FollowService follows;

	FollowController(FollowService follows) {
		this.follows = follows;
	}

	record FollowResponse(Relation relation) {
	}

	@PostMapping("/api/profiles/{handle}/follow")
	FollowResponse follow(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		return new FollowResponse(follows.follow(userId(jwt), handle));
	}

	@DeleteMapping("/api/profiles/{handle}/follow")
	ResponseEntity<Void> unfollow(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		follows.unfollow(userId(jwt), handle);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/me/follow-requests")
	List<ProfileCard> pendingRequests(@AuthenticationPrincipal Jwt jwt) {
		return follows.pendingRequests(userId(jwt));
	}

	@PostMapping("/api/me/follow-requests/{handle}/accept")
	ResponseEntity<Void> accept(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		follows.acceptRequest(userId(jwt), handle);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/api/me/follow-requests/{handle}")
	ResponseEntity<Void> reject(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		follows.rejectRequest(userId(jwt), handle);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/me/duollowers")
	List<ProfileCard> followers(@AuthenticationPrincipal Jwt jwt) {
		return follows.followers(userId(jwt));
	}

	@DeleteMapping("/api/me/duollowers/{handle}")
	ResponseEntity<Void> removeFollower(@AuthenticationPrincipal Jwt jwt, @PathVariable String handle) {
		follows.removeFollower(userId(jwt), handle);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/me/duollowing")
	List<ProfileCard> following(@AuthenticationPrincipal Jwt jwt) {
		return follows.following(userId(jwt));
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
