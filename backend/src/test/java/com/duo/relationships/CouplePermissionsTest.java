package com.duo.relationships;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** A matriz de permissões por estado (RN-11/12/12a/13a) — tabela em `docs/epico-2-vinculo.md`, Etapa 11. */
class CouplePermissionsTest {

	@Test
	void coupleCenterIsVisibleWhileActiveOrPaused() {
		assertThat(CouplePermissions.canViewCoupleCenter(CoupleState.ACTIVE)).isTrue();
		assertThat(CouplePermissions.canViewCoupleCenter(CoupleState.PAUSED)).isTrue();
		assertThat(CouplePermissions.canViewCoupleCenter(CoupleState.ENDED)).isFalse();
	}

	@Test
	void postingToTheCoupleCenterOnlyWorksWhileActive() {
		assertThat(CouplePermissions.canPostToCoupleCenter(CoupleState.ACTIVE)).isTrue();
		assertThat(CouplePermissions.canPostToCoupleCenter(CoupleState.PAUSED)).isFalse();
		assertThat(CouplePermissions.canPostToCoupleCenter(CoupleState.ENDED)).isFalse();
	}

	@Test
	void postingToOwnSideOnlyWorksWhileActive() {
		assertThat(CouplePermissions.canPostToOwnSide(CoupleState.ACTIVE)).isTrue();
		assertThat(CouplePermissions.canPostToOwnSide(CoupleState.PAUSED)).isFalse();
		assertThat(CouplePermissions.canPostToOwnSide(CoupleState.ENDED)).isFalse();
	}

	@Test
	void personalOnlyComesBackWhilePaused() {
		assertThat(CouplePermissions.canUsePersonal(CoupleState.ACTIVE)).isFalse();
		assertThat(CouplePermissions.canUsePersonal(CoupleState.PAUSED)).isTrue();
		assertThat(CouplePermissions.canUsePersonal(CoupleState.ENDED)).isFalse();
	}
}
