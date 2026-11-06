package com.duo.social;

import java.util.List;

import com.duo.social.dto.ProfileCard;
import com.duo.social.dto.Relation;
import com.duo.social.exception.FollowLimitException;
import com.duo.social.exception.FollowRequestNotFoundException;
import com.duo.social.exception.InvalidSocialActionException;
import com.duo.social.exception.ProfileNotFoundException;

/**
 * Duollowers (quem me segue) e Duollowing (quem eu sigo). Perfil público: seguir já vale. Perfil privado: vira
 * um pedido que o dono aceita ou recusa. Recusar, remover e deixar de seguir nunca avisam a outra pessoa.
 */
public interface FollowService {

	/**
	 * @throws ProfileNotFoundException se a pessoa não existe ou não está visível para quem pede (bloqueio)
	 * @throws InvalidSocialActionException se for a própria pessoa
	 * @throws FollowLimitException se já há pedidos pendentes demais
	 */
	Relation follow(Long me, String handle);

	/** Deixa de seguir ou cancela o pedido pendente; sem erro se já não seguia. */
	void unfollow(Long me, String handle);

	/** @throws FollowRequestNotFoundException se essa pessoa não tem um pedido pendente para mim */
	void acceptRequest(Long me, String followerHandle);

	void rejectRequest(Long me, String followerHandle);

	void removeFollower(Long me, String followerHandle);

	/** Pedidos de seguir que recebi e ainda não respondi. */
	List<ProfileCard> pendingRequests(Long me);

	List<ProfileCard> followers(Long me);

	List<ProfileCard> following(Long me);
}
