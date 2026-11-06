package com.duo.social;

/**
 * O que um visitante pode enxergar do perfil de outra pessoa — a regra central de acesso do Épico 3. Cada tela
 * ou módulo que mostra conteúdo de uma pessoa pergunta isto, em vez de decidir sozinho (se cada um decidisse,
 * um dia um esqueceria uma regra).
 *
 * <ul>
 * <li>{@code OWNER}: é o próprio dono; vê tudo.</li>
 * <li>{@code FOLLOWER}: é Duollower aceito; vê o conteúdo marcado para Duollowers ou públicos.</li>
 * <li>{@code PUBLIC_VIEWER}: não segue, mas o perfil é público; vê só o conteúdo público.</li>
 * <li>{@code OUTSIDER}: não segue e o perfil é privado; vê só o cartão básico.</li>
 * <li>{@code UNAVAILABLE}: bloqueio (de qualquer lado), perfil oculto pela moderação ou conta inexistente;
 * para o visitante a pessoa simplesmente não existe.</li>
 * </ul>
 */
public enum Access {
	OWNER, FOLLOWER, PUBLIC_VIEWER, OUTSIDER, UNAVAILABLE;

	/** Nome, DuoName, foto, bio e estado civil: o cartão básico que todo mundo (não bloqueado) vê. */
	public boolean canSeeCard() {
		return this != UNAVAILABLE;
	}

	/** Pode ver ao menos o conteúdo público do perfil (fotos e momentos). */
	public boolean canSeeContent() {
		return this == OWNER || this == FOLLOWER || this == PUBLIC_VIEWER;
	}
}
