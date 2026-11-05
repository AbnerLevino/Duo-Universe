package com.duo.relationships;

/** RN-25: pelo menos um dos 4 passos da história ainda está vazio. */
public class StoryIncompleteException extends RuntimeException {

	public StoryIncompleteException() {
		super("A história ainda não está completa.");
	}
}
