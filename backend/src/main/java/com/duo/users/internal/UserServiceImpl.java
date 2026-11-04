package com.duo.users.internal;

import java.time.Instant;
import java.util.Optional;

import com.duo.users.UserService;
import com.duo.users.UserSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class UserServiceImpl implements UserService {

	private static final int MAX_DISPLAY_NAME = 100;

	private final UserRepository userRepository;

	UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserSummary findOrCreateByGoogle(String googleSub, String email, String displayName) {
		User user = userRepository.findByGoogleSub(googleSub)
			.orElseGet(() -> createFromGoogle(googleSub, email, displayName));
		return toSummary(user);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSummary> findByGoogleSub(String googleSub) {
		return userRepository.findByGoogleSub(googleSub).map(UserServiceImpl::toSummary);
	}

	private User createFromGoogle(String googleSub, String email, String displayName) {
		User user = new User(googleSub, email, nameOrFallback(displayName, email));
		user.markEmailVerified(Instant.now());
		return userRepository.save(user);
	}

	private static String nameOrFallback(String displayName, String email) {
		String name = (displayName == null || displayName.isBlank())
			? email.substring(0, email.indexOf('@'))
			: displayName.trim();
		return name.length() > MAX_DISPLAY_NAME ? name.substring(0, MAX_DISPLAY_NAME) : name;
	}

	private static UserSummary toSummary(User user) {
		return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName());
	}
}
