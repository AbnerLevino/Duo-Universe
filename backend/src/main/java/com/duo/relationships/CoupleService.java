package com.duo.relationships;

/**
 * API pública do Couple Universe depois de nascido: pausar, retomar e sair (RN-13 a RN-18). O nascimento em
 * si (a partir de um {@code CouplePairing} confirmado) chega na Etapa 8, por {@code CouplePairingService}.
 */
public interface CoupleService {

	/**
	 * Busca um casal que a pessoa possa ver — só membros atuais (RN-12).
	 *
	 * @throws CoupleNotFoundException se não existir ou se {@code userId} não for membro ativo dele
	 */
	CoupleSummary find(Long coupleId, Long userId);

	/**
	 * RN-13: qualquer membro pausa sozinho; nada é apagado. Só funciona a partir de {@code ACTIVE}.
	 *
	 * @throws CoupleNotFoundException se não existir ou se {@code userId} não for membro ativo dele
	 * @throws IllegalCoupleTransitionException se o casal não estiver {@code ACTIVE}
	 */
	CoupleSummary pause(Long coupleId, Long userId);

	/**
	 * Qualquer membro retoma; só funciona a partir de {@code PAUSED}.
	 *
	 * @throws CoupleNotFoundException se não existir ou se {@code userId} não for membro ativo dele
	 * @throws IllegalCoupleTransitionException se o casal não estiver {@code PAUSED}
	 */
	CoupleSummary resume(Long coupleId, Long userId);

	/**
	 * RN-14/15/17/18: qualquer membro sai sozinho, a qualquer momento ({@code ACTIVE} ou {@code PAUSED}). No
	 * MVP (RN-07, exatamente 2 membros), sair termina o vínculo inteiro para os dois — o centro do casal em
	 * si nunca é apagado (RN-17), só o estado vira {@code ENDED}.
	 *
	 * @throws CoupleNotFoundException se não existir ou se {@code userId} não for membro ativo dele
	 * @throws IllegalCoupleTransitionException se o casal já estiver {@code ENDED}
	 */
	void leave(Long coupleId, Long userId);
}
