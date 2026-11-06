package com.duo.social.dto;

/** O cartão básico de uma pessoa (listas e resultados de busca). Nunca traz e-mail. */
public record ProfileCard(Long userId, String displayName, String handle, String bio, String profession,
		Long avatarMediaId) {
}
