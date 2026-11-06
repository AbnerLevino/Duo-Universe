package com.duo.media.internal.service;

import com.duo.media.MediaKind;
import com.duo.media.MediaVisibility;
import com.duo.social.Access;

/**
 * A regra de quem vê uma foto (RN-19), numa função pura (sem banco, sem Spring) para ser testada em todas as
 * combinações. Parte do {@link Access} do visitante, que já considera bloqueio, perfil oculto e perfil privado:
 * a foto só pode ser <em>mais</em> restrita do que o perfil, nunca menos.
 */
final class PhotoAccessRule {

	private PhotoAccessRule() {
	}

	static boolean canView(MediaKind kind, MediaVisibility visibility, Access access) {
		return switch (access) {
			case OWNER -> true;
			case UNAVAILABLE -> false;
			// a foto de perfil faz parte do cartão básico, que todo mundo (não bloqueado) vê
			case FOLLOWER, PUBLIC_VIEWER, OUTSIDER -> kind == MediaKind.AVATAR || switch (access) {
				case FOLLOWER -> visibility == MediaVisibility.DUOLLOWERS || visibility == MediaVisibility.PUBLIC;
				case PUBLIC_VIEWER -> visibility == MediaVisibility.PUBLIC;
				default -> false;
			};
		};
	}
}
