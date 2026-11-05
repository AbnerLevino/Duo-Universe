package com.duo.relationships;

import com.duo.shared.RateLimitExceededException;

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

	/**
	 * Convite pelo caminho padrão: busca B por um @handle exato (RN-30). Handle inexistente, handle da
	 * própria pessoa que está convidando, e handle de alguém já em vínculo ACTIVE/PAUSED caem na mesma
	 * exceção genérica (RA-07/DEC-031) — não revela qual dos três motivos é o real.
	 *
	 * @throws HandleNotInvitableException se o handle não puder ser convidado agora
	 * @throws RateLimitExceededException se {@code initiatorUserId} excedeu as tentativas permitidas (RA-08)
	 */
	PairingSummary inviteByHandle(Long initiatorUserId, String handle);

	/** Qualquer uma das duas pessoas pode desistir, a qualquer momento, enquanto o pareamento está PENDING. */
	void cancel(Long pairingId, Long userId);

	/**
	 * Gera um convite por link: um pareamento {@code PENDING} sem convidado ainda, mais um código aleatório
	 * de uso único, válido por 7 dias (P1, alternativa ao @handle da Etapa 5). O código em texto puro só
	 * existe no retorno — o banco guarda só o hash (RA-09).
	 */
	IssuedPairingInvite inviteByLink(Long initiatorUserId);

	/**
	 * Usa o código do link: anexa {@code inviteeUserId} ao pareamento que o código identifica.
	 *
	 * @throws InviteCodeInvalidException se o código não existir, já tiver sido usado, estiver vencido, ou
	 *         se a própria pessoa que criou o link tentar usá-lo
	 */
	PairingSummary redeemByLink(String code, Long inviteeUserId);

	/**
	 * Busca um pareamento que a pessoa possa ver. Se ele já passou da validade e ainda estava
	 * {@code PENDING}, marca como {@code CANCELLED} nesta mesma chamada (expiração "lazy": sem job
	 * agendado, decisão da Etapa 2).
	 *
	 * @throws PairingNotFoundException se não existir ou se {@code userId} não fizer parte dele
	 */
	PairingSummary find(Long pairingId, Long userId);
}
