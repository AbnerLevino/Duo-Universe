package com.duo.auth.internal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import com.duo.auth.RefreshTokenService;
import com.duo.auth.dto.IssuedRefreshToken;
import com.duo.auth.internal.model.RefreshToken;
import com.duo.auth.internal.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RefreshTokenServiceImpl implements RefreshTokenService {

	/** RA-04: o token de renovação vale 30 dias. */
	static final Duration TTL = Duration.ofDays(30);

	private static final int TOKEN_BYTES = 32;

	private final SecureRandom random = new SecureRandom();
	private final RefreshTokenRepository tokens;
	private final Clock clock;

	RefreshTokenServiceImpl(RefreshTokenRepository tokens, Clock clock) {
		this.tokens = tokens;
		this.clock = clock;
	}

	@Override
	@Transactional
	public IssuedRefreshToken issue(Long userId) {
		return create(userId, UUID.randomUUID()).issued();
	}

	/**
	 * Devolve vazio (em vez de lançar exceção) porque, no reuso, a revogação da família precisa ser gravada:
	 * uma exceção desfaria a transação inteira e o ladrão continuaria com a sessão.
	 */
	@Override
	@Transactional
	public Optional<IssuedRefreshToken> rotate(String rawToken) {
		Instant now = clock.instant();
		Optional<RefreshToken> found = tokens.findForUpdateByTokenHash(hash(rawToken));
		if (found.isEmpty()) {
			return Optional.empty();
		}
		RefreshToken current = found.get();
		if (current.isRevoked()) {
			// RA-05: token antigo reapresentado = foi copiado; encerra todas as sessões dessa família
			tokens.revokeFamily(current.getFamilyId(), now);
			return Optional.empty();
		}
		if (current.isExpired(now)) {
			return Optional.empty();
		}
		Created next = create(current.getUserId(), current.getFamilyId());
		current.revoke(now, next.entity().getId());
		return Optional.of(next.issued());
	}

	@Override
	@Transactional
	public void revoke(String rawToken) {
		tokens.findByTokenHash(hash(rawToken))
			.ifPresent(token -> tokens.revokeFamily(token.getFamilyId(), clock.instant()));
	}

	@Override
	@Transactional
	public void revokeAll(Long userId) {
		tokens.revokeAllOfUser(userId, clock.instant());
	}

	private Created create(Long userId, UUID familyId) {
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		Instant expiresAt = clock.instant().plus(TTL);
		RefreshToken saved = tokens.save(new RefreshToken(userId, hash(raw), familyId, expiresAt));
		return new Created(saved, new IssuedRefreshToken(userId, raw, expiresAt));
	}

	static String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 não está disponível", e);
		}
	}

	private record Created(RefreshToken entity, IssuedRefreshToken issued) {
	}
}
