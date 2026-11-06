package com.duo.relationships;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.duo.relationships.dto.IssuedPairingInvite;
import com.duo.relationships.dto.PairingSummary;
import com.duo.relationships.dto.StorySummary;
import com.duo.relationships.exception.AlreadyInRelationshipException;
import com.duo.relationships.exception.DatesNotAcceptedException;
import com.duo.relationships.exception.HandleNotInvitableException;
import com.duo.relationships.exception.InviteCodeInvalidException;
import com.duo.relationships.exception.PairingNotFoundException;
import com.duo.relationships.exception.StoryIncompleteException;
import com.duo.shared.exception.RateLimitExceededException;

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

	/**
	 * Grava (ou atualiza) um dos 4 passos da história de {@code userId} (RN-25). Não valida tamanho nenhum —
	 * o rascunho pode ficar incompleto enquanto a pessoa ainda está escrevendo.
	 *
	 * @throws PairingNotFoundException se o pareamento não existir ou {@code userId} não fizer parte dele
	 */
	void saveStoryDraft(Long pairingId, Long userId, StoryStep step, String text);

	/** RN-25: true quando os 4 passos da história de {@code userId} têm texto (nenhum vazio). */
	boolean isStoryComplete(Long pairingId, Long userId);

	/**
	 * A mesma checagem de {@link #isStoryComplete}, mas falhando alto — pensado para a Etapa 8 usar na hora
	 * de "firmar" o vínculo.
	 *
	 * @throws StoryIncompleteException se algum dos 4 passos ainda estiver vazio
	 */
	void requireStoryComplete(Long pairingId, Long userId);

	/**
	 * A história do parceiro de {@code userId} neste pareamento — só devolvida quando **as duas** histórias
	 * (a própria e a do parceiro) já estiverem completas (RN-26); antes disso, ou se ainda não há parceiro
	 * (convite por link não usado), devolve vazio.
	 *
	 * @throws PairingNotFoundException se o pareamento não existir ou {@code userId} não fizer parte dele
	 */
	Optional<StorySummary> readPartnerStory(Long pairingId, Long userId);

	/**
	 * Propõe as duas datas do casal (quando se conheceram, quando começaram a namorar) — P3/RN-27. Qualquer
	 * proposta nova (inclusive uma contraproposta) reabre o consenso: descarta uma aceitação anterior.
	 *
	 * @throws PairingNotFoundException se o pareamento não existir ou {@code userId} não fizer parte dele
	 */
	void proposeDates(Long pairingId, Long userId, LocalDate knownSinceDate, LocalDate datingSinceDate);

	/**
	 * A outra pessoa (nunca quem propôs) aceita a proposta de datas em vigor.
	 *
	 * @throws PairingNotFoundException se o pareamento não existir ou {@code userId} não fizer parte dele
	 */
	void acceptDates(Long pairingId, Long userId);

	/** RN-27: true quando as duas datas do casal já foram aceitas. */
	boolean areDatesAccepted(Long pairingId, Long userId);

	/**
	 * A mesma checagem de {@link #areDatesAccepted}, mas falhando alto — pensado para a Etapa 8 usar na hora
	 * de "firmar" o vínculo.
	 *
	 * @throws DatesNotAcceptedException se as datas ainda não tiverem sido aceitas
	 */
	void requireDatesAccepted(Long pairingId, Long userId);

	/**
	 * RN-08: {@code userId} confirma a sua parte do vínculo ("firmar"). Quando as duas pessoas já
	 * confirmaram, o {@code Couple} nasce (vazio — RN-06/RN-07) e o pareamento vira {@code CONFIRMED}; só
	 * depois disso é que história ({@link #saveStoryDraft}) e datas ({@link #proposeDates}) podem ser
	 * escritas (decisão de 2026-11-05: firmar primeiro, perguntas depois).
	 *
	 * @throws PairingNotFoundException se o pareamento não existir ou {@code userId} não fizer parte dele
	 * @throws AlreadyInRelationshipException se {@code userId} já estiver em outro vínculo ACTIVE/PAUSED
	 *         (RN-06) no instante em que o Couple tentar nascer — a confirmação de {@code userId} continua
	 *         registrada mesmo assim
	 */
	PairingSummary confirm(Long pairingId, Long userId);

	/** Etapa 13: os pareamentos ainda {@code PENDING} onde {@code userId} participa — é o que o app consulta
	 * ao abrir, para saber se há um convite esperando resposta. */
	List<PairingSummary> findPendingForUser(Long userId);
}
