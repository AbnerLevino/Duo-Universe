package com.duo.media;

/**
 * Quem pode ver uma foto (RN-19). Vale em conjunto com a visibilidade do perfil: num perfil privado, nem a foto
 * pública é vista por quem não é Duollower. {@code COUPLE} entra junto com o Couple Universe.
 */
public enum MediaVisibility {
	ONLY_ME, DUOLLOWERS, PUBLIC
}
