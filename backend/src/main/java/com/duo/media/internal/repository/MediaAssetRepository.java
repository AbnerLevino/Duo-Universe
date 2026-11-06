package com.duo.media.internal.repository;

import java.util.List;
import java.util.Optional;

import com.duo.media.MediaKind;
import com.duo.media.internal.model.MediaAsset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

	List<MediaAsset> findByOwnerUserIdAndKindOrderByCreatedAtDescIdDesc(Long ownerUserId, MediaKind kind);

	Optional<MediaAsset> findFirstByOwnerUserIdAndKind(Long ownerUserId, MediaKind kind);

	long countByOwnerUserIdAndKind(Long ownerUserId, MediaKind kind);

	/** Espaço já usado pelas fotos de uma pessoa (para a cota). */
	@Query("select coalesce(sum(m.sizeBytes), 0) from MediaAsset m where m.ownerUserId = :ownerUserId and m.kind = :kind")
	long sumSizeBytes(Long ownerUserId, MediaKind kind);
}
