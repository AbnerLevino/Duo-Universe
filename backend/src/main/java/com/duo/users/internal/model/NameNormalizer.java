package com.duo.users.internal.model;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Deixa um nome pronto para a busca (Etapa 4): minúsculas, sem acento, sem espaço sobrando. Assim "João  Silva"
 * e "joao silva" viram o mesmo texto, e a busca fica um simples {@code LIKE} no banco. O mesmo tratamento vale
 * para o que a pessoa digita na busca; por isso ele mora num lugar só.
 */
public final class NameNormalizer {

	private NameNormalizer() {
	}

	public static String normalize(String text) {
		if (text == null) {
			return "";
		}
		String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return withoutAccents.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
	}
}
