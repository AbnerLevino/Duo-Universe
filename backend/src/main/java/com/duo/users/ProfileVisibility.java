package com.duo.users;

/**
 * Quem enxerga o conteúdo do Personal Universe de uma pessoa (Épico 3). O cartão básico (nome, DuoName, foto,
 * bio) é visível a qualquer um nos dois casos; o que muda é o conteúdo: {@code PUBLIC} qualquer pessoa vê o que
 * a pessoa marcou como público; {@code PRIVATE} só quem ela aprovou como Duollower. O Personal nasce público
 * (decisão do fundador, 2026-11-06).
 */
public enum ProfileVisibility {
	PUBLIC, PRIVATE
}
