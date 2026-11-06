package com.duo.social;

import java.util.List;

import com.duo.social.dto.ProfileCard;

/** Bloqueio: quem bloqueia some para o bloqueado e vice-versa, e os Duollows entre os dois deixam de existir. */
public interface BlockService {

	void block(Long me, String handle);

	void unblock(Long me, String handle);

	List<ProfileCard> blocked(Long me);
}
