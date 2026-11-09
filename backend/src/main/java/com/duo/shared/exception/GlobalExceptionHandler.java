package com.duo.shared.exception;

import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduz as exceções de negócio do projeto em respostas HTTP, num lugar só (decisão do fundador, Etapa 13,
 * regra 8) — sem isso, cada controller novo (fotos, memórias, ...) repetiria o mesmo {@code try/catch}.
 * {@link ProblemDetail} é do próprio Spring (desde a versão 6, já incluso) — não é uma dependência nova; é
 * um formato de erro HTTP padronizado, em vez de eu inventar um JSON próprio (regra 9). Cada exceção de
 * negócio já sabe o próprio status HTTP ({@link DuoHttpException#httpStatus()}) — este handler só aplica.
 *
 * <p>RF-A08: as três últimas regras abaixo pegam falhas previstas que não têm exceção de negócio própria
 * (um {@code orElseThrow()} sem resultado, um argumento inválido, uma regra do banco violada) e devolvem um
 * erro do cliente em vez de um 500 — que, além de feio, sugere "o servidor quebrou" quando a requisição é que
 * estava errada. A mensagem é sempre fixa: nunca devolvemos o texto da exceção, que pode citar SQL ou dados.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(DuoHttpException.class)
	ProblemDetail handle(DuoHttpException ex) {
		return ProblemDetail.forStatusAndDetail(ex.httpStatus(), ex.getMessage());
	}

	@ExceptionHandler(NoSuchElementException.class)
	ProblemDetail handleNotFound(NoSuchElementException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Não encontrado");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleBadArgument(IllegalArgumentException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requisição inválida");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleConflict(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflito com o estado atual dos dados");
	}
}
