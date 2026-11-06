package com.duo.media.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.duo.media.MediaKind;
import com.duo.media.MediaVisibility;
import com.duo.social.Access;

import org.junit.jupiter.api.Test;

/** RN-19: a matriz inteira de quem vê cada foto, sem banco nem Spring. */
class PhotoAccessRuleTest {

	private static boolean photo(MediaVisibility visibility, Access access) {
		return PhotoAccessRule.canView(MediaKind.PHOTO, visibility, access);
	}

	@Test
	void theOwnerSeesEverything() {
		for (MediaVisibility visibility : MediaVisibility.values()) {
			assertThat(photo(visibility, Access.OWNER)).isTrue();
		}
	}

	@Test
	void nobodyElseSeesAnOnlyMePhoto() {
		for (Access access : new Access[] { Access.FOLLOWER, Access.PUBLIC_VIEWER, Access.OUTSIDER,
				Access.UNAVAILABLE }) {
			assertThat(photo(MediaVisibility.ONLY_ME, access)).isFalse();
		}
	}

	@Test
	void aFollowersPhotoIsSeenByFollowersOnly() {
		assertThat(photo(MediaVisibility.DUOLLOWERS, Access.FOLLOWER)).isTrue();
		assertThat(photo(MediaVisibility.DUOLLOWERS, Access.PUBLIC_VIEWER)).isFalse();
		assertThat(photo(MediaVisibility.DUOLLOWERS, Access.OUTSIDER)).isFalse();
	}

	@Test
	void aPublicPhotoNeedsAPublicProfileOrBeingAFollower() {
		assertThat(photo(MediaVisibility.PUBLIC, Access.FOLLOWER)).isTrue();
		assertThat(photo(MediaVisibility.PUBLIC, Access.PUBLIC_VIEWER)).isTrue();
		// perfil privado: nem a foto pública é vista por quem não foi aprovado
		assertThat(photo(MediaVisibility.PUBLIC, Access.OUTSIDER)).isFalse();
	}

	@Test
	void blockedOrHiddenPeopleSeeNothingEvenPublicPhotosOrTheAvatar() {
		assertThat(photo(MediaVisibility.PUBLIC, Access.UNAVAILABLE)).isFalse();
		assertThat(PhotoAccessRule.canView(MediaKind.AVATAR, MediaVisibility.PUBLIC, Access.UNAVAILABLE)).isFalse();
	}

	@Test
	void theAvatarIsPartOfTheCardSoEveryoneNotBlockedSeesIt() {
		for (Access access : new Access[] { Access.OWNER, Access.FOLLOWER, Access.PUBLIC_VIEWER, Access.OUTSIDER }) {
			assertThat(PhotoAccessRule.canView(MediaKind.AVATAR, MediaVisibility.ONLY_ME, access)).isTrue();
		}
	}
}
