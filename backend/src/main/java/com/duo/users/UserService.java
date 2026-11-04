package com.duo.users;

import java.util.Optional;

/** API pública do módulo {@code users}: os outros módulos só falam com ele por aqui. */
public interface UserService {

	/**
	 * Devolve o usuário dono deste {@code googleSub}, criando a conta no primeiro acesso (DEC-030).
	 * O e-mail já vem verificado pelo Google (RA-01). O nome só é usado na criação: depois disso
	 * ele pertence à pessoa e o Google não o sobrescreve.
	 */
	UserSummary findOrCreateByGoogle(String googleSub, String email, String displayName);

	Optional<UserSummary> findByGoogleSub(String googleSub);
}
