package com.duo.relationships;

/** RN-07: o MVP tem exatamente 2 membros por casal. Os estados do {@code Couple} (ver a matriz em
 * {@code docs/epico-2-vinculo.md}): {@code ACTIVE} ⇄ {@code PAUSED}, e de qualquer um dos dois para
 * {@code ENDED} (final — RN-18, nunca volta). */
public enum CoupleState {
	ACTIVE, PAUSED, ENDED
}
