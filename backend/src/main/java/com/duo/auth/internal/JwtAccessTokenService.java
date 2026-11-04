package com.duo.auth.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.duo.auth.AccessToken;
import com.duo.auth.AccessTokenService;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
class JwtAccessTokenService implements AccessTokenService {

	/** RA-04: o token de acesso vale 15 minutos. */
	static final Duration TTL = Duration.ofMinutes(15);

	private final JwtEncoder encoder;
	private final Clock clock;

	JwtAccessTokenService(JwtEncoder encoder, Clock clock) {
		this.encoder = encoder;
		this.clock = clock;
	}

	/** O token carrega só o id do usuário (sem e-mail, nome ou qualquer dado pessoal). */
	@Override
	public AccessToken issue(Long userId) {
		Instant now = clock.instant();
		Instant expiresAt = now.plus(TTL);
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(JwtConfig.ISSUER)
			.subject(userId.toString())
			.issuedAt(now)
			.expiresAt(expiresAt)
			.build();
		String value = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();
		return new AccessToken(value, expiresAt);
	}
}
