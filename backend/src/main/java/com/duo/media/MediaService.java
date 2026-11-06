package com.duo.media;

import java.util.List;
import java.util.Optional;

import com.duo.media.dto.MediaContent;
import com.duo.media.dto.MediaSummary;
import com.duo.media.dto.UploadedFile;
import com.duo.media.exception.InvalidMediaException;
import com.duo.media.exception.MediaNotFoundException;
import com.duo.media.exception.MediaQuotaExceededException;
import com.duo.social.ReportReason;
import com.duo.social.exception.ProfileNotFoundException;

/** API pública do módulo {@code media}. A pessoa que age é sempre a do token, nunca um id vindo do app. */
public interface MediaService {

	/**
	 * Troca a foto de perfil (até 1 MB; a anterior é apagada).
	 *
	 * @throws InvalidMediaException se o arquivo não for um JPEG, PNG ou WebP de verdade ou for grande demais
	 */
	MediaSummary uploadAvatar(Long userId, UploadedFile file);

	void removeAvatar(Long userId);

	/**
	 * Guarda um momento no Personal Universe (até 2 MB), por padrão visível só para a própria pessoa.
	 *
	 * @throws MediaQuotaExceededException se passou do limite provisório de fotos ou de espaço
	 */
	MediaSummary uploadPhoto(Long userId, UploadedFile file, String caption, MediaVisibility visibility);

	/** @throws MediaNotFoundException se a foto não é do usuário (a existência não é revelada) */
	MediaSummary updatePhoto(Long userId, Long mediaId, String caption, MediaVisibility visibility);

	void deletePhoto(Long userId, Long mediaId);

	/** O arquivo, se {@code viewerId} puder vê-lo; vazio se não existe <em>ou</em> não é para ele. */
	Optional<MediaContent> openFor(Long viewerId, Long mediaId);

	/** As fotos de {@code ownerId} que {@code viewerId} pode ver, da mais nova para a mais antiga. */
	List<MediaSummary> photosOf(Long viewerId, Long ownerId);

	/** @throws ProfileNotFoundException se o perfil dono da foto não está visível para quem pede */
	void reportPhoto(Long reporterId, Long mediaId, ReportReason reason, String details);
}
