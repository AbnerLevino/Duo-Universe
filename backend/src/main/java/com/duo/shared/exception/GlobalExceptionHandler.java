package com.duo.shared.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz as exceções de negócio do projeto em respostas HTTP, num lugar só (decisão do fundador, Etapa 13,
 * regra 8) — sem isso, cada controller novo (fotos, memórias, ...) repetiria o mesmo {@code try/catch}.
 * {@link ProblemDetail} é do próprio Spring (desde a versão 6, já incluso) — não é uma dependência nova; é
 * um formato de erro HTTP padronizado, em vez de eu inventar um JSON próprio (regra 9). Cada exceção de
 * negócio já sabe o próprio status HTTP ({@link DuoHttpException#httpStatus()}) — este handler só aplica.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(DuoHttpException.class)
	ProblemDetail handle(DuoHttpException ex) {
		return ProblemDetail.forStatusAndDetail(ex.httpStatus(), ex.getMessage());
	}
}
