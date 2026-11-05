package com.duo.shared;

/** Contrato para qualquer coisa que "mora" num {@link Scope} (P4/RN-09) — fotos e memórias vão implementar
 * isso quando existirem (Semanas 4 e 5); por ora, só testes unitários com objetos fictícios o usam. */
public interface Scopeable {

	Scope scope();
}
