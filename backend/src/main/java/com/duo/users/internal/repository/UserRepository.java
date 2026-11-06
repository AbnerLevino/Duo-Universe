package com.duo.users.internal.repository;

import java.util.Collection;
import java.util.Optional;

import com.duo.users.internal.model.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByGoogleSub(String googleSub);

	Optional<User> findByHandleIgnoreCase(String handle);

	/**
	 * Busca por DuoName que começa com o termo ou nome que o contém (os padrões já vêm com os curingas e
	 * escapados com {@code !}). O DuoName exato vem primeiro. Slice, e não Page: só precisamos saber se há uma página
	 * seguinte, sem pagar um COUNT.
	 */
	@Query("""
			select u from User u
			where u.deletedAt is null and u.hiddenAt is null and u.id not in :excludedIds
			  and (lower(u.handle) like :handlePattern escape '!' or u.searchName like :namePattern escape '!')
			order by case when lower(u.handle) = :exact then 0 else 1 end, u.displayName, u.id
			""")
	Slice<User> search(String exact, String handlePattern, String namePattern, Collection<Long> excludedIds,
			Pageable pageable);
}
