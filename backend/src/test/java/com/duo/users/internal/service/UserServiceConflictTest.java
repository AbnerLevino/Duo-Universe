package com.duo.users.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/** Os dois casos de borda do primeiro login (pendência de segurança do passo 5/6). */
class UserServiceConflictTest {

	private final UserRepository repository = mock(UserRepository.class);
	private final UserServiceImpl service = new UserServiceImpl(repository, java.time.Clock.systemUTC());

	@Test
	void simultaneousFirstLoginReturnsTheAccountTheOtherRequestCreated() {
		User created = new User("sub-1", "ana@example.com", "Ana");
		when(repository.findByGoogleSub("sub-1")).thenReturn(Optional.empty()).thenReturn(Optional.of(created));
		when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());
		when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("users_google_sub_unique"));

		UserSummary result = service.findOrCreateByGoogle("sub-1", "ana@example.com", "Ana");

		assertThat(result.email()).isEqualTo("ana@example.com");
	}

	@Test
	void emailOfAnotherAccountIsRejectedBeforeInserting() {
		when(repository.findByGoogleSub("sub-novo")).thenReturn(Optional.empty());
		when(repository.findByEmailIgnoreCase("ana@example.com"))
			.thenReturn(Optional.of(new User("sub-antigo", "ana@example.com", "Ana")));

		assertThatThrownBy(() -> service.findOrCreateByGoogle("sub-novo", "ana@example.com", "Ana"))
			.isInstanceOf(EmailAlreadyLinkedException.class);
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void emailConflictRaisedByTheDatabaseIsAlsoRejected() {
		when(repository.findByGoogleSub("sub-novo")).thenReturn(Optional.empty());
		when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());
		when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("users_email_unique"));

		assertThatThrownBy(() -> service.findOrCreateByGoogle("sub-novo", "ana@example.com", "Ana"))
			.isInstanceOf(EmailAlreadyLinkedException.class);
	}
}
