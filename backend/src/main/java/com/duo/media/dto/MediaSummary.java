package com.duo.media.dto;

import java.time.Instant;

import com.duo.media.MediaKind;
import com.duo.media.MediaVisibility;

/** O que o app sabe de uma foto (nunca o caminho no armazenamento). O arquivo se pede por {@code /api/media/{id}}. */
public record MediaSummary(Long id, Long ownerUserId, MediaKind kind, MediaVisibility visibility, String caption,
		String contentType, long sizeBytes, Instant createdAt) {
}
