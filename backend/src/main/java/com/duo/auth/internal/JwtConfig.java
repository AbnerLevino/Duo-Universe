package com.duo.auth.internal;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Assina e valida o token de acesso com HMAC-SHA256 e um segredo só do Duo ({@code DUO_JWT_SECRET}, regra 11).
 * A aplicação não sobe sem o segredo ou com um segredo curto demais.
 */
@Configuration
public class JwtConfig {

	public static final String ISSUER = "duo";
	private static final int MIN_SECRET_BYTES = 32;

	@Bean
	JwtEncoder jwtEncoder(@Value("${duo.jwt.secret}") String secret) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key(secret)));
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${duo.jwt.secret}") String secret) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key(secret)).macAlgorithm(MacAlgorithm.HS256).build();
		// expiração e emissor são conferidos em toda requisição
		OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(ISSUER);
		decoder.setJwtValidator(validator);
		return decoder;
	}

	private static SecretKey key(String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("DUO_JWT_SECRET precisa ter pelo menos " + MIN_SECRET_BYTES + " caracteres");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}
}
