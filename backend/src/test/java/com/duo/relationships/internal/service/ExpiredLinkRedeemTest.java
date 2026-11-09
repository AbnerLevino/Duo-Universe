package com.duo.relationships.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import com.duo.relationships.CouplePairingService;
import com.duo.relationships.dto.IssuedPairingInvite;
import com.duo.relationships.exception.InviteCodeInvalidException;
import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * RF-A10: resgatar um link vencido responde "inválido" <b>e</b> o cancelamento do convite fica gravado. Precisa do
 * contexto de verdade ({@code @SpringBootTest}): o desfazer da transação por causa da exceção só acontece
 * através do proxy do Spring.
 */
@ActiveProfiles("test")
@SpringBootTest
class ExpiredLinkRedeemTest {

	@Autowired
	CouplePairingService pairings;

	@Autowired
	UserService users;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void redeemingAnExpiredLinkStillPersistsTheCancellation() {
		String unique = UUID.randomUUID().toString();
		UserSummary a = users.findOrCreateByGoogle("sub-a-" + unique, "a-" + unique + "@example.com", "A");
		UserSummary b = users.findOrCreateByGoogle("sub-b-" + unique, "b-" + unique + "@example.com", "B");
		try {
			IssuedPairingInvite invite = pairings.inviteByLink(a.id());
			jdbc.update("update couple_pairings set expires_at = now() - interval '1 day' where id = ?", invite.pairingId());

			assertThatThrownBy(() -> pairings.redeemByLink(invite.code(), b.id()))
				.isInstanceOf(InviteCodeInvalidException.class);

			String state = jdbc.queryForObject("select state from couple_pairings where id = ?", String.class,
					invite.pairingId());
			assertThat(state).isEqualTo("CANCELLED");
		}
		finally {
			jdbc.update("delete from couple_pairing_stories where pairing_id in (select id from couple_pairings where initiator_user_id = ?)", a.id());
			jdbc.update("delete from couple_pairings where initiator_user_id = ?", a.id());
			jdbc.update("delete from users where id in (?, ?)", a.id(), b.id());
		}
	}
}
