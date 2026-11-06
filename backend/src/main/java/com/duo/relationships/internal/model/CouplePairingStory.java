package com.duo.relationships.internal.model;

import com.duo.relationships.StoryStep;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A história de uma pessoa sobre um pareamento, em 4 passos (RN-25, RN-26). Nasce vazia — é o rascunho — e vai
 * sendo preenchida um passo de cada vez por {@link #updateStep}; nada aqui valida tamanho, só se o passo tem
 * algum texto (ver {@link #isComplete}).
 */
@Entity
@Table(name = "couple_pairing_stories")
public class CouplePairingStory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pairing_id", nullable = false, updatable = false)
	private Long pairingId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@Column(name = "how_we_met")
	private String howWeMet;

	@Column(name = "time_before_dating")
	private String timeBeforeDating;

	@Column(name = "what_i_thought_then")
	private String whatIThoughtThen;

	@Column(name = "what_i_think_now")
	private String whatIThinkNow;

	protected CouplePairingStory() {
	}

	public CouplePairingStory(Long pairingId, Long userId) {
		this.pairingId = pairingId;
		this.userId = userId;
	}

	public void updateStep(StoryStep step, String text) {
		switch (step) {
			case HOW_WE_MET -> howWeMet = text;
			case TIME_BEFORE_DATING -> timeBeforeDating = text;
			case WHAT_I_THOUGHT_THEN -> whatIThoughtThen = text;
			case WHAT_I_THINK_NOW -> whatIThinkNow = text;
		}
	}

	/** RN-25: completa quando os 4 passos têm texto, sem contar espaços nas pontas — sem mínimo de tamanho. */
	public boolean isComplete() {
		return hasText(howWeMet) && hasText(timeBeforeDating) && hasText(whatIThoughtThen) && hasText(whatIThinkNow);
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	public Long getId() {
		return id;
	}

	public Long getPairingId() {
		return pairingId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getHowWeMet() {
		return howWeMet;
	}

	public String getTimeBeforeDating() {
		return timeBeforeDating;
	}

	public String getWhatIThoughtThen() {
		return whatIThoughtThen;
	}

	public String getWhatIThinkNow() {
		return whatIThinkNow;
	}
}
