package com.duo.users.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.duo.users.UserService;
import com.duo.users.dto.ProfilePage;
import com.duo.users.dto.ProfileUpdate;
import com.duo.users.dto.UserProfile;
import com.duo.users.dto.UserSummary;
import com.duo.users.exception.EmailAlreadyLinkedException;
import com.duo.users.exception.HandleChangeLimitException;
import com.duo.users.exception.HandleTakenException;
import com.duo.users.exception.InvalidHandleException;
import com.duo.users.exception.InvalidProfileException;
import com.duo.users.exception.InvalidSearchException;
import com.duo.users.internal.model.NameNormalizer;
import com.duo.users.internal.model.User;
import com.duo.users.internal.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserServiceImpl implements UserService {

	private static final int MAX_DISPLAY_NAME_FROM_GOOGLE = 100;
	private static final int MIN_NAME = 2;
	private static final int MAX_NAME = 50;
	private static final int MAX_BIO = 100;
	private static final int MAX_PROFESSION = 20;
	private static final int MIN_SEARCH_LENGTH = 3;
	private static final int MAX_HANDLE_CHANGES_PER_DAY = 2;

	private static final Pattern HANDLE_FORMAT = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

	private final UserRepository userRepository;
	private final Clock clock;

	UserServiceImpl(UserRepository userRepository, Clock clock) {
		this.userRepository = userRepository;
		this.clock = clock;
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
		String current = user.getHandle();
		if (current != null && current.equalsIgnoreCase(handle)) {
			// Mudar só maiúsculas/minúsculas não "gasta" uma das 2 trocas do dia
			user.setHandle(handle);
		}
		else {
			if (current != null) {
				LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
				boolean alreadyChangedToday = today.equals(user.getHandleChangesOn());
				if (alreadyChangedToday && user.getHandleChangesToday() >= MAX_HANDLE_CHANGES_PER_DAY) {
					throw new HandleChangeLimitException();
				}
				user.recordHandleChange(today);
			}
			user.setHandle(handle);
		}
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

	@Override
	@Transactional(readOnly = true)
	public Optional<UserSummary> findActiveByHandle(String handle) {
		return userRepository.findByHandleIgnoreCase(handle)
			.filter(user -> user.getDeletedAt() == null && user.getHiddenAt() == null)
			.map(UserServiceImpl::toSummary);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserProfile> findProfileById(Long id) {
		return userRepository.findById(id).filter(user -> user.getDeletedAt() == null).map(UserServiceImpl::toProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserProfile> findProfileByHandle(String handle) {
		return userRepository.findByHandleIgnoreCase(handle)
			.filter(user -> user.getDeletedAt() == null)
			.map(UserServiceImpl::toProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserProfile> findProfilesByIds(Collection<Long> ids) {
		return userRepository.findAllById(ids)
			.stream()
			.filter(user -> user.getDeletedAt() == null)
			.map(UserServiceImpl::toProfile)
			.toList();
	}

	@Override
	@Transactional
	public UserProfile updateProfile(Long userId, ProfileUpdate update) {
		String name = update.displayName() == null ? "" : update.displayName().trim();
		requireLength(name, MIN_NAME, MAX_NAME, "O nome deve ter de 2 a 50 caracteres");
		String bio = blankToNull(update.bio());
		String profession = blankToNull(update.profession());
		if (bio != null) {
			requireLength(bio, 0, MAX_BIO, "A bio pode ter até 100 caracteres");
		}
		if (profession != null) {
			requireLength(profession, 0, MAX_PROFESSION, "A profissão pode ter até 20 caracteres");
		}
		if (update.maritalStatus() == null || update.profileVisibility() == null) {
			throw new InvalidProfileException("Informe o estado civil e a visibilidade do perfil");
		}
		User user = userRepository.findById(userId).filter(u -> u.getDeletedAt() == null).orElseThrow();
		user.updateProfile(name, bio, profession, update.maritalStatus(), update.profileVisibility());
		return toProfile(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public ProfilePage searchProfiles(String query, Collection<Long> excludedIds, int page, int size) {
		String term = NameNormalizer.normalize(query);
		if (term.codePointCount(0, term.length()) < MIN_SEARCH_LENGTH) {
			throw new InvalidSearchException();
		}
		String escaped = escapeLike(term);
		// "not in ()" vazio quebra em alguns bancos; um id que nunca existe resolve
		Collection<Long> excluded = excludedIds.isEmpty() ? List.of(-1L) : excludedIds;
		Slice<User> found = userRepository.search(term, escaped + "%", "%" + escaped + "%", excluded,
				PageRequest.of(page, size));
		return new ProfilePage(found.getContent().stream().map(UserServiceImpl::toProfile).toList(), found.hasNext());
	}

	@Override
	@Transactional
	public void setAvatar(Long userId, Long mediaId) {
		User user = userRepository.findById(userId).orElseThrow();
		user.setAvatarMediaId(mediaId);
		userRepository.save(user);
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
		return name.length() > MAX_DISPLAY_NAME_FROM_GOOGLE ? name.substring(0, MAX_DISPLAY_NAME_FROM_GOOGLE) : name;
	}

	private static void requireLength(String text, int min, int max, String message) {
		int length = text.codePointCount(0, text.length());
		if (length < min || length > max) {
			throw new InvalidProfileException(message);
		}
	}

	private static String blankToNull(String text) {
		return text == null || text.isBlank() ? null : text.trim();
	}

	/** Os curingas do LIKE ({@code %} e {@code _}) viram texto comum: "_" é válido num DuoName. */
	private static String escapeLike(String term) {
		return term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
	}

	private static UserSummary toSummary(User user) {
		return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getHandle());
	}

	private static UserProfile toProfile(User user) {
		return new UserProfile(user.getId(), user.getDisplayName(), user.getHandle(), user.getBio(),
				user.getProfession(), user.getMaritalStatus(), user.getProfileVisibility(), user.getAvatarMediaId(),
				user.getHiddenAt() != null);
	}
}
