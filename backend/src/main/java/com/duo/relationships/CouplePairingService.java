package com.duo.relationships;

/**
 * API pública do convite para o vínculo (DEC-012). Nesta etapa: só nascer, cancelar e expirar — o convite de
 * verdade por @handle/link chega nas Etapas 3 e 5; confirmar os dois lados e nascer o {@code Couple} chega na
 * Etapa 8.
 */
public interface CouplePairingService {

	/**
	 * Cria um pareamento {@code PENDING}, válido por 7 dias (RN-29). {@code inviteeUserId} pode ser nulo
	 * quando o convite ainda não tem destinatário certo (ex.: por link, antes de ser usado — Etapa 3).
	 */
	PairingSummary initiate(Long initiatorUserId, Long inviteeUserId, Channel channel);

	/** Qualquer uma das duas pessoas pode desistir, a qualquer momento, enquanto o pareamento está PENDING. */
	void cancel(Long pairingId, Long userId);

	/**
	 * Busca um pareamento que a pessoa possa ver. Se ele já passou da validade e ainda estava
	 * {@code PENDING}, marca como {@code CANCELLED} nesta mesma chamada (expiração "lazy": sem job
	 * agendado, decisão da Etapa 2).
	 *
	 * @throws PairingNotFoundException se não existir ou se {@code userId} não fizer parte dele
	 */
	PairingSummary find(Long pairingId, Long userId);
}
