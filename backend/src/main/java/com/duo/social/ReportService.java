package com.duo.social;

/**
 * Denúncia (seção 16 do CLAUDE.md). A revisão é manual no MVP; aqui só se registra, sem revelar quem denunciou.
 * Denunciar duas vezes o mesmo alvo é aceito em silêncio (idempotente).
 */
public interface ReportService {

	void reportProfile(Long reporterId, String handle, ReportReason reason, String details);

	/** Quem chama (o módulo de mídias) já conferiu que o denunciante pode ver essa foto. */
	void reportMedia(Long reporterId, Long ownerId, Long mediaId, ReportReason reason, String details);
}
