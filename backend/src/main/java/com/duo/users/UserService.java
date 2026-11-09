package com.duo.users;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.duo.users.dto.ProfilePage;
import com.duo.users.dto.ProfileUpdate;
import com.duo.users.dto.UserProfile;
import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;
import com.duo.users.exception.HandleTakenException;
import com.duo.users.exception.HandleChangeLimitException;
import com.duo.users.exception.InvalidHandleException;
import com.duo.users.exception.InvalidProfileException;
import com.duo.users.exception.InvalidSearchException;

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
	 * @throws HandleChangeLimitException se a pessoa já trocou o handle 2 vezes hoje (a primeira escolha e
	 *         mudar só maiúsculas/minúsculas não contam)
	 */
	UserSummary chooseHandle(Long userId, String handle);

	Optional<UserSummary> findByHandle(String handle);

	/**
	 * Igual a {@link #findByHandle}, mas só devolve contas que podem receber um convite de vínculo: vazio se a
	 * conta foi excluída ou o perfil está oculto pela moderação (RF-A01).
	 */
	Optional<UserSummary> findActiveByHandle(String handle);

	/** O perfil de uma pessoa (sem e-mail); vazio se a conta não existe ou foi excluída. */
	Optional<UserProfile> findProfileById(Long id);

	Optional<UserProfile> findProfileByHandle(String handle);

	/** Vários perfis de uma vez (listas de Duollowers); ids que não existem são ignorados. */
	List<UserProfile> findProfilesByIds(Collection<Long> ids);

	/**
	 * Troca o que a pessoa edita no próprio perfil (Épico 3).
	 *
	 * @throws InvalidProfileException se algum campo estiver fora das regras
	 */
	UserProfile updateProfile(Long userId, ProfileUpdate update);

	/**
	 * Busca por nome ou DuoName (decisão do fundador, B3/B4). Não diferencia maiúsculas nem acentos. Perfis
	 * ocultos e as contas em {@code excludedIds} não aparecem.
	 *
	 * @throws InvalidSearchException se o termo tiver menos de 3 letras
	 */
	ProfilePage searchProfiles(String query, Collection<Long> excludedIds, int page, int size);

	/** Define (ou, com {@code null}, remove) a foto de perfil; quem chama é o módulo de mídias. */
	void setAvatar(Long userId, Long mediaId);
}
