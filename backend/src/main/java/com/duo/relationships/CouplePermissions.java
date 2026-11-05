package com.duo.relationships;

/**
 * A matriz de permissões por estado do vínculo (RN-11, RN-12, RN-12a, RN-13a — tabela em
 * {@code docs/epico-2-vinculo.md}). São consultas puras sobre o {@link CoupleState}: quem chama já garantiu,
 * por outro caminho (ex.: {@code CoupleService}), que a pessoa é membro do casal (RN-12/RN-20) — esta classe
 * não sabe nada de usuário, só responde "o quê" é permitido em cada estado.
 */
public final class CouplePermissions {

	private CouplePermissions() {
	}

	/** RN-12: o centro do casal pode ser visto em ACTIVE e PAUSED; não em ENDED. */
	public static boolean canViewCoupleCenter(CoupleState state) {
		return state != CoupleState.ENDED;
	}

	/** RN-11: postar ou editar no centro do casal — só com o vínculo ACTIVE. */
	public static boolean canPostToCoupleCenter(CoupleState state) {
		return state == CoupleState.ACTIVE;
	}

	/** RN-12a: postar no próprio lado dentro do casal — só com o vínculo ACTIVE. */
	public static boolean canPostToOwnSide(CoupleState state) {
		return state == CoupleState.ACTIVE;
	}

	/**
	 * RN-13a (revisão da DEC-002, decidida em 2026-11-05): durante a {@code PAUSED}, a pessoa volta a poder
	 * ver e postar/editar no próprio Personal Universe, por livre escolha. Enquanto {@code ACTIVE}, não existe
	 * um "eu" separado (DEC-002) — por isso é {@code false} nesse estado.
	 */
	public static boolean canUsePersonal(CoupleState state) {
		return state == CoupleState.PAUSED;
	}
}
