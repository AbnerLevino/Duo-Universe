package com.duo.auth;

import java.util.Optional;

import com.duo.auth.dto.IssuedRefreshToken;

/** API pública do token de renovação (RA-04, RA-05): os outros módulos só falam com ele por aqui. */
public interface RefreshTokenService {

	/** Abre uma sessão nova (família nova) para o usuário, ex.: logo após o login com Google. */
	IssuedRefreshToken issue(Long userId);

	/**
	 * Troca um token por outro novo da mesma família (RA-05). Devolve vazio se o token é desconhecido, expirou
	 * ou já foi usado; no caso de reuso, toda a família é encerrada.
	 */
	Optional<IssuedRefreshToken> rotate(String rawToken);

	/** "Sair deste aparelho" (H4): encerra a família do token. Silencioso se o token não existir. */
	void revoke(String rawToken);

	/** "Sair de todos os aparelhos" (H4). */
	void revokeAll(Long userId);
}
