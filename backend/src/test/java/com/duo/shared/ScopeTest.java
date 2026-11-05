package com.duo.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** P4/RN-09: só a estrutura do "o que levar" — nenhuma tabela ou endpoint usa isso ainda (Etapa 9). */
class ScopeTest {

	/** Objeto fictício só para provar que o contrato {@link Scopeable} funciona — nenhuma entidade real o
	 * implementa ainda (fotos só chegam na Semana 4). */
	private record FakeItem(Scope scope) implements Scopeable {
	}

	@Test
	void personalAndArchivedAreNotVisibleToThePartner() {
		assertThat(new FakeItem(Scope.PERSONAL).scope().isVisibleToPartner()).isFalse();
		assertThat(new FakeItem(Scope.ARCHIVED).scope().isVisibleToPartner()).isFalse();
	}

	@Test
	void memberSideAndCoupleCenterAreVisibleToThePartner() {
		assertThat(new FakeItem(Scope.MEMBER_SIDE).scope().isVisibleToPartner()).isTrue();
		assertThat(new FakeItem(Scope.COUPLE_CENTER).scope().isVisibleToPartner()).isTrue();
	}
}
