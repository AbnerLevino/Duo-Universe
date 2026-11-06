package com.duo.social.exception;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/**
 * O perfil não existe, está oculto pela moderação ou há bloqueio — sempre a mesma resposta, para ninguém
 * descobrir (pelo tipo de erro) que foi bloqueado ou que a pessoa existe (no espírito da RA-07).
 */
public class ProfileNotFoundException extends DuoHttpException {

	public ProfileNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Perfil não encontrado");
	}
}
