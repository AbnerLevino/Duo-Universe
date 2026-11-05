package com.duo.relationships.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.duo.relationships.StoryStep;

import org.junit.jupiter.api.Test;

/** RN-25: completude da história, sem banco envolvido. */
class CouplePairingStoryTest {

	private CouplePairingStory story() {
		return new CouplePairingStory(1L, 2L);
	}

	@Test
	void startsIncomplete() {
		assertThat(story().isComplete()).isFalse();
	}

	@Test
	void isIncompleteWhileAnyStepIsMissing() {
		CouplePairingStory story = story();

		story.updateStep(StoryStep.HOW_WE_MET, "Nos conhecemos numa festa.");
		story.updateStep(StoryStep.TIME_BEFORE_DATING, "Conversamos por meses.");
		story.updateStep(StoryStep.WHAT_I_THOUGHT_THEN, "Achei engraçada.");

		assertThat(story.isComplete()).isFalse();
	}

	@Test
	void isCompleteOnceAllFourStepsHaveText() {
		CouplePairingStory story = story();

		story.updateStep(StoryStep.HOW_WE_MET, "Nos conhecemos numa festa.");
		story.updateStep(StoryStep.TIME_BEFORE_DATING, "Conversamos por meses.");
		story.updateStep(StoryStep.WHAT_I_THOUGHT_THEN, "Achei engraçada.");
		story.updateStep(StoryStep.WHAT_I_THINK_NOW, "Acho ainda mais hoje.");

		assertThat(story.isComplete()).isTrue();
	}

	@Test
	void blankTextDoesNotCountAsAnAnswer() {
		CouplePairingStory story = story();

		story.updateStep(StoryStep.HOW_WE_MET, "   ");
		story.updateStep(StoryStep.TIME_BEFORE_DATING, "Conversamos por meses.");
		story.updateStep(StoryStep.WHAT_I_THOUGHT_THEN, "Achei engraçada.");
		story.updateStep(StoryStep.WHAT_I_THINK_NOW, "Acho ainda mais hoje.");

		assertThat(story.isComplete()).isFalse();
	}

	@Test
	void doesNotEnforceAnyMinimumLength() {
		CouplePairingStory story = story();

		story.updateStep(StoryStep.HOW_WE_MET, "oi");
		story.updateStep(StoryStep.TIME_BEFORE_DATING, "oi");
		story.updateStep(StoryStep.WHAT_I_THOUGHT_THEN, "oi");
		story.updateStep(StoryStep.WHAT_I_THINK_NOW, "oi");

		assertThat(story.isComplete()).isTrue();
	}

	@Test
	void updatingAStepAgainReplacesTheText() {
		CouplePairingStory story = story();
		story.updateStep(StoryStep.HOW_WE_MET, "primeira versão");

		story.updateStep(StoryStep.HOW_WE_MET, "versão final");

		assertThat(story.getHowWeMet()).isEqualTo("versão final");
	}
}
