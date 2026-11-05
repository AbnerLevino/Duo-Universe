package com.duo.shared;

/**
 * Onde uma coisa "mora" dentro do Duo (P4/RN-09, {@code docs/epico-2-vinculo.md}). Fotos (Semana 4) e
 * memórias (Semana 5) vão usar isso para saber quem pode ver o quê. Mora em {@code shared} porque não é
 * regra do vínculo — é algo que todo módulo com conteúdo do usuário vai precisar entender. Nesta etapa é só a
 * estrutura: nenhuma tabela ou endpoint usa isso ainda.
 */
public enum Scope {

	/** Do Personal Universe de quem não está vinculado, ou de quem está, mas ainda não escolheu o que levar. */
	PERSONAL,

	/** Ficou para trás no momento do vínculo (RN-09): privado, invisível ao parceiro, volta se a pessoa sair. */
	ARCHIVED,

	/** O lado de uma pessoa dentro do casal — ela posta, o parceiro vê (RN-12a). */
	MEMBER_SIDE,

	/** O centro do casal — o que é de "nós" (RN-11). */
	COUPLE_CENTER;

	/** RN-09/RN-12a: só o que está do lado de alguém ou no centro do casal é visível ao parceiro. */
	public boolean isVisibleToPartner() {
		return this == MEMBER_SIDE || this == COUPLE_CENTER;
	}
}
