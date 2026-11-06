package com.duo.users;

import java.util.Optional;

import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;
import com.duo.users.exception.HandleTakenException;
import com.duo.users.exception.InvalidHandleException;

/** API pública do módulo {@code users}: os outros módulos só falam com ele por aqui. */
public interface UserService {

	/**
	 * Devolve o usuário dono deste {@code googleSub}, criando a conta no primeiro acesso (DEC-030).
	 * O e-mail já vem verificado pelo Google (RA-01). O nome só é usado na criação: depois disso
	 * ele pertence à pessoa e o Google não o sobrescreve.
	 *
	 * @throws EmailAlreadyLinkedException se o e-mail já pertence a uma conta de outro {@code googleSub}
	 */
	UserSummary findOrCreateByGoogle(String googleSub, String email, String displayName);

	Optional<UserSummary> findByGoogleSub(String googleSub);

	/** Usado pela sessão: o token de acesso carrega o id do usuário, nunca o e-mail. */
	Optional<UserSummary> findById(Long id);

	/**
	 * Escolhe o @handle desta pessoa (RN-30), usado para ser encontrada num convite de vínculo.
	 * Comparação sem diferenciar maiúsculas, como o e-mail.
	 *
	 * @throws InvalidHandleException  se o formato não tiver 3 a 20 letras, números ou "_"
	 * @throws HandleTakenException   se o handle já pertence a outra conta
	 */
	UserSummary chooseHandle(Long userId, String handle);

	Optional<UserSummary> findByHandle(String handle);
}
