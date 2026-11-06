package com.duo.users.internal.repository;

import java.util.Optional;

import com.duo.users.internal.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByGoogleSub(String googleSub);

	Optional<User> findByHandleIgnoreCase(String handle);
}
