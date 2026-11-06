package com.duo.users.internal.service;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

import com.duo.users.UserService;
import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;
import com.duo.users.exception.HandleTakenException;
import com.duo.users.exception.InvalidHandleException;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserServiceImpl implements UserService {

	private static final int MAX_DISPLAY_NAME = 100;

	private static final Pattern HANDLE_FORMAT = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

	private final UserRepository userRepository;

	UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Sem transação própria de propósito: se duas requisições criam a mesma conta ao mesmo tempo, o índice
	 * único faz uma delas falhar no {@code saveAndFlush}; dentro de uma transação externa isso a deixaria
	 * inutilizável e não daria para buscar a conta que a outra requisição acabou de criar.
	 */
	@Override
	public UserSummary findOrCreateByGoogle(String googleSub, String email, String displayName) {
		Optional<User> existing = userRepository.findByGoogleSub(googleSub);
		if (existing.isPresent()) {
			return toSummary(existing.get());
		}
		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new EmailAlreadyLinkedException();
		}
		try {
			return toSummary(userRepository.saveAndFlush(newGoogleUser(googleSub, email, displayName)));
		}
		catch (DataIntegrityViolationException raceOrConflict) {
			return userRepository.findByGoogleSub(googleSub)
				.map(UserServiceImpl::toSummary)
				.orElseThrow(EmailAlreadyLinkedException::new);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSummary> findByGoogleSub(String googleSub) {
		return userRepository.findByGoogleSub(googleSub).map(UserServiceImpl::toSummary);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSummary> findById(Long id) {
		return userRepository.findById(id).filter(user -> user.getDeletedAt() == null).map(UserServiceImpl::toSummary);
	}

	@Override
	public UserSummary chooseHandle(Long userId, String handle) {
		if (handle == null || !HANDLE_FORMAT.matcher(handle).matches()) {
			throw new InvalidHandleException();
		}
		User user = userRepository.findById(userId).orElseThrow();
		user.setHandle(handle);
		try {
			return toSummary(userRepository.saveAndFlush(user));
		}
		catch (DataIntegrityViolationException alreadyTaken) {
			throw new HandleTakenException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSummary> findByHandle(String handle) {
		return userRepository.findByHandleIgnoreCase(handle).map(UserServiceImpl::toSummary);
	}

	private static User newGoogleUser(String googleSub, String email, String displayName) {
		User user = new User(googleSub, email, nameOrFallback(displayName, email));
		user.markEmailVerified(Instant.now());
		return user;
	}

	private static String nameOrFallback(String displayName, String email) {
		String name = (displayName == null || displayName.isBlank())
			? email.substring(0, email.indexOf('@'))
			: displayName.trim();
		return name.length() > MAX_DISPLAY_NAME ? name.substring(0, MAX_DISPLAY_NAME) : name;
	}

	private static UserSummary toSummary(User user) {
		return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getHandle());
	}
}
