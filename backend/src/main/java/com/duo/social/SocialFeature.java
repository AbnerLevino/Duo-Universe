package com.duo.social;

/**
 * Nome da chave de configuração que liga a camada social (busca, Duollowers, perfil de terceiros e fotos de
 * terceiros). RF-A11: o produto passou a ter foco no casal e essa camada está <b>congelada</b> — o código e as
 * tabelas continuam, mas nada disso responde nem aparece. Para religar: {@code duo.features.social.enabled=true}.
 */
public final class SocialFeature {

	public static final String PROPERTY = "duo.features.social.enabled";

	private SocialFeature() {
	}
}
