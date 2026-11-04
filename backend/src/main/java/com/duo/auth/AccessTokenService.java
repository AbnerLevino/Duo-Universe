package com.duo.auth;

/** Emite o token de acesso de 15 minutos (RA-04). Quem o valida é o Spring Security, em cada requisição. */
public interface AccessTokenService {

	AccessToken issue(Long userId);
}
