package com.duo.media.internal.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.duo.media.MediaKind;
import com.duo.media.MediaService;
import com.duo.media.MediaVisibility;
import com.duo.media.dto.MediaContent;
import com.duo.media.dto.MediaSummary;
import com.duo.media.dto.UploadedFile;
import com.duo.media.exception.InvalidMediaException;
import com.duo.media.exception.MediaNotFoundException;
import com.duo.media.exception.MediaQuotaExceededException;
import com.duo.media.internal.model.MediaAsset;
import com.duo.media.internal.repository.MediaAssetRepository;
import com.duo.shared.RateLimiter;
import com.duo.shared.Scope;
import com.duo.social.Access;
import com.duo.social.ProfileAccessService;
import com.duo.social.ReportReason;
import com.duo.social.ReportService;
import com.duo.social.exception.ProfileNotFoundException;
import com.duo.storage.MediaStorageProvider;
import com.duo.users.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MediaServiceImpl implements MediaService {

	static final int MAX_AVATAR_BYTES = 1024 * 1024;
	static final int MAX_PHOTO_BYTES = 2 * 1024 * 1024;
	private static final int MAX_CAPTION = 200;
	private static final String UPLOAD_SCOPE = "media_upload";
	private static final int MAX_UPLOADS_PER_HOUR = 60;

	private final MediaAssetRepository assets;
	private final MediaStorageProvider storage;
	private final UserService users;
	private final ProfileAccessService access;
	private final ReportService reports;
	private final RateLimiter rateLimiter;
	private final int maxPhotos;
	private final long maxTotalBytes;

	MediaServiceImpl(MediaAssetRepository assets, MediaStorageProvider storage, UserService users,
			ProfileAccessService access, ReportService reports, RateLimiter rateLimiter,
			@Value("${duo.media.max-photos:200}") int maxPhotos,
			@Value("${duo.media.max-total-bytes:104857600}") long maxTotalBytes) {
		this.assets = assets;
		this.storage = storage;
		this.users = users;
		this.access = access;
		this.reports = reports;
		this.rateLimiter = rateLimiter;
		this.maxPhotos = maxPhotos;
		this.maxTotalBytes = maxTotalBytes;
	}

	@Override
	@Transactional
	public MediaSummary uploadAvatar(Long userId, UploadedFile file) {
		rateLimiter.checkAndRecord(UPLOAD_SCOPE, userId.toString(), MAX_UPLOADS_PER_HOUR, Duration.ofHours(1));
		String contentType = validate(file, MAX_AVATAR_BYTES);
		Optional<MediaAsset> previous = assets.findFirstByOwnerUserIdAndKind(userId, MediaKind.AVATAR);
		MediaAsset saved = storeAndSave(userId, MediaKind.AVATAR, MediaVisibility.PUBLIC, null, file, contentType);
		users.setAvatar(userId, saved.getId());
		previous.ifPresent(this::deleteAsset);
		return toSummary(saved);
	}

	@Override
	@Transactional
	public void removeAvatar(Long userId) {
		users.setAvatar(userId, null);
		assets.findFirstByOwnerUserIdAndKind(userId, MediaKind.AVATAR).ifPresent(this::deleteAsset);
	}

	@Override
	@Transactional
	public MediaSummary uploadPhoto(Long userId, UploadedFile file, String caption, MediaVisibility visibility) {
		rateLimiter.checkAndRecord(UPLOAD_SCOPE, userId.toString(), MAX_UPLOADS_PER_HOUR, Duration.ofHours(1));
		String contentType = validate(file, MAX_PHOTO_BYTES);
		String cleanCaption = cleanCaption(caption);
		if (assets.countByOwnerUserIdAndKind(userId, MediaKind.PHOTO) >= maxPhotos
				|| assets.sumSizeBytes(userId, MediaKind.PHOTO) + file.bytes().length > maxTotalBytes) {
			throw new MediaQuotaExceededException();
		}
		MediaVisibility chosen = visibility == null ? MediaVisibility.ONLY_ME : visibility;
		return toSummary(storeAndSave(userId, MediaKind.PHOTO, chosen, cleanCaption, file, contentType));
	}

	@Override
	@Transactional
	public MediaSummary updatePhoto(Long userId, Long mediaId, String caption, MediaVisibility visibility) {
		MediaAsset photo = ownedPhoto(userId, mediaId);
		photo.edit(cleanCaption(caption), visibility == null ? photo.getVisibility() : visibility);
		return toSummary(assets.save(photo));
	}

	@Override
	@Transactional
	public void deletePhoto(Long userId, Long mediaId) {
		deleteAsset(ownedPhoto(userId, mediaId));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MediaContent> openFor(Long viewerId, Long mediaId) {
		return assets.findById(mediaId)
			.filter(asset -> canView(viewerId, asset))
			.flatMap(asset -> storage.read(asset.getStorageKey())
				.map(bytes -> new MediaContent(bytes, asset.getContentType(), asset.getSha256())));
	}

	@Override
	@Transactional(readOnly = true)
	public List<MediaSummary> photosOf(Long viewerId, Long ownerId) {
		Access viewerAccess = access.accessOf(viewerId, ownerId);
		if (!viewerAccess.canSeeCard()) {
			throw new ProfileNotFoundException();
		}
		return assets.findByOwnerUserIdAndKindOrderByCreatedAtDescIdDesc(ownerId, MediaKind.PHOTO)
			.stream()
			.filter(asset -> canView(viewerAccess, asset))
			.map(MediaServiceImpl::toSummary)
			.toList();
	}

	/**
	 * Sem transação própria de propósito: uma denúncia repetida bate no índice único dentro do
	 * {@code ReportService}, que a aceita em silêncio; numa transação externa essa falha marcaria tudo como
	 * "só reverter" e a requisição acabaria em erro mesmo com a exceção capturada.
	 */
	@Override
	public void reportPhoto(Long reporterId, Long mediaId, ReportReason reason, String details) {
		MediaAsset asset = assets.findById(mediaId)
			.filter(found -> canView(reporterId, found))
			.orElseThrow(MediaNotFoundException::new);
		reports.reportMedia(reporterId, asset.getOwnerUserId(), asset.getId(), reason, details);
	}

	private boolean canView(Long viewerId, MediaAsset asset) {
		return canView(access.accessOf(viewerId, asset.getOwnerUserId()), asset);
	}

	/** Uma foto oculta pela moderação só é vista pela própria dona; as demais seguem a regra de acesso. */
	private static boolean canView(Access viewerAccess, MediaAsset asset) {
		if (asset.getHiddenAt() != null && viewerAccess != Access.OWNER) {
			return false;
		}
		return PhotoAccessRule.canView(asset.getKind(), asset.getVisibility(), viewerAccess);
	}

	private MediaAsset ownedPhoto(Long userId, Long mediaId) {
		return assets.findById(mediaId)
			.filter(asset -> asset.getOwnerUserId().equals(userId) && asset.getKind() == MediaKind.PHOTO)
			.orElseThrow(MediaNotFoundException::new);
	}

	/**
	 * Grava o arquivo e depois o registro. Se o registro falhar, o arquivo recém-gravado seria um órfão para
	 * sempre; por isso é apagado de volta antes de repassar o erro.
	 */
	private MediaAsset storeAndSave(Long userId, MediaKind kind, MediaVisibility visibility, String caption,
			UploadedFile file, String contentType) {
		String key = "users/" + userId + "/" + kind.name().toLowerCase() + "-" + UUID.randomUUID() + "."
				+ ImageSniffer.extensionOf(contentType);
		storage.store(key, file.bytes());
		try {
			return assets.saveAndFlush(new MediaAsset(userId, kind, Scope.PERSONAL, visibility, key, contentType,
					file.bytes().length, sha256(file.bytes()), caption));
		}
		catch (RuntimeException failure) {
			storage.delete(key);
			throw failure;
		}
	}

	private void deleteAsset(MediaAsset asset) {
		assets.delete(asset);
		storage.delete(asset.getStorageKey());
	}

	private static String validate(UploadedFile file, int maxBytes) {
		if (file == null || file.bytes() == null || file.bytes().length == 0) {
			throw new InvalidMediaException("Escolha uma imagem para enviar");
		}
		if (file.bytes().length > maxBytes) {
			throw new InvalidMediaException(
					"A imagem é grande demais (máximo " + (maxBytes / (1024 * 1024)) + " MB)");
		}
		String contentType = ImageSniffer.detect(file.bytes());
		if (contentType == null) {
			throw new InvalidMediaException("Formato não suportado. Use JPEG, PNG ou WebP");
		}
		return contentType;
	}

	private static String cleanCaption(String caption) {
		if (caption == null || caption.isBlank()) {
			return null;
		}
		String trimmed = caption.trim();
		if (trimmed.codePointCount(0, trimmed.length()) > MAX_CAPTION) {
			throw new InvalidMediaException("A legenda pode ter até 200 caracteres");
		}
		return trimmed;
	}

	private static String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 sempre existe na JVM", e);
		}
	}

	private static MediaSummary toSummary(MediaAsset asset) {
		return new MediaSummary(asset.getId(), asset.getOwnerUserId(), asset.getKind(), asset.getVisibility(),
				asset.getCaption(), asset.getContentType(), asset.getSizeBytes(), asset.getCreatedAt());
	}
}
