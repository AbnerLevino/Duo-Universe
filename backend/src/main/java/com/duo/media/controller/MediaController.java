package com.duo.media.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.duo.media.MediaService;
import com.duo.media.MediaVisibility;
import com.duo.media.dto.MediaContent;
import com.duo.media.dto.MediaSummary;
import com.duo.media.dto.UploadedFile;
import com.duo.media.exception.MediaNotFoundException;
import com.duo.social.ReportReason;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Foto de perfil e momentos do Personal. O arquivo só sai por aqui, depois de o backend conferir quem pede
 * (RN-19/RN-21): não existe link aberto para a foto. Quem pede é sempre a pessoa do token (regra 11).
 */
@RestController
class MediaController {

	private final MediaService media;

	MediaController(MediaService media) {
		this.media = media;
	}

	record EditPhotoRequest(String caption, MediaVisibility visibility) {
	}

	record ReportPhotoRequest(ReportReason reason, String details) {
	}

	@PostMapping("/api/me/avatar")
	MediaSummary uploadAvatar(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file)
			throws IOException {
		return media.uploadAvatar(userId(jwt), new UploadedFile(file.getBytes()));
	}

	@DeleteMapping("/api/me/avatar")
	ResponseEntity<Void> removeAvatar(@AuthenticationPrincipal Jwt jwt) {
		media.removeAvatar(userId(jwt));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/api/me/photos")
	MediaSummary uploadPhoto(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file,
			@RequestParam(name = "caption", required = false) String caption,
			@RequestParam(name = "visibility", required = false) MediaVisibility visibility) throws IOException {
		return media.uploadPhoto(userId(jwt), new UploadedFile(file.getBytes()), caption, visibility);
	}

	@PatchMapping("/api/media/{id}")
	MediaSummary edit(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody EditPhotoRequest request) {
		return media.updatePhoto(userId(jwt), id, request.caption(), request.visibility());
	}

	@DeleteMapping("/api/media/{id}")
	ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		media.deletePhoto(userId(jwt), id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/media/{id}")
	ResponseEntity<byte[]> content(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		MediaContent content = media.openFor(userId(jwt), id).orElseThrow(MediaNotFoundException::new);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(content.contentType()))
			// o navegador nunca "adivinha" outro tipo para o arquivo (defesa contra conteúdo disfarçado)
			.header("X-Content-Type-Options", "nosniff")
			// private: só o navegador de quem pediu guarda; nunca um cache compartilhado
			.cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
			.eTag("\"" + content.sha256() + "\"")
			.body(content.bytes());
	}

	@GetMapping("/api/media/users/{ownerId}/photos")
	List<MediaSummary> photosOf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long ownerId) {
		return media.photosOf(userId(jwt), ownerId);
	}

	@PostMapping("/api/media/{id}/report")
	ResponseEntity<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@RequestBody ReportPhotoRequest request) {
		media.reportPhoto(userId(jwt), id, request.reason(), request.details());
		return ResponseEntity.noContent().build();
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
