package com.duo.relationships;

import com.duo.shared.exception.DuoHttpException;

import org.springframework.http.HttpStatus;

/** RN-25: pelo menos um dos 4 passos da história ainda está vazio. */
public class StoryIncompleteException extends DuoHttpException {

	public StoryIncompleteException() {
		super(HttpStatus.CONFLICT, "A história ainda não está completa.");
	}
}
