package com.duo.social.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.duo.social.internal.model.Follow;
import com.duo.social.internal.model.FollowStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

	List<Follow> findByFollowerIdAndFolloweeIdIn(Long followerId, Collection<Long> followeeIds);

	long countByFolloweeIdAndStatus(Long followeeId, FollowStatus status);

	long countByFollowerIdAndStatus(Long followerId, FollowStatus status);

	List<Follow> findTop200ByFolloweeIdAndStatusOrderByCreatedAtDesc(Long followeeId, FollowStatus status);

	List<Follow> findTop200ByFollowerIdAndStatusOrderByCreatedAtDesc(Long followerId, FollowStatus status);

	@Modifying
	@Query("delete from Follow f where f.followerId = :followerId and f.followeeId = :followeeId")
	int deleteByPair(Long followerId, Long followeeId);

	/** Bloquear remove o Duollow nos dois sentidos. */
	@Modifying
	@Query("""
			delete from Follow f
			where (f.followerId = :a and f.followeeId = :b) or (f.followerId = :b and f.followeeId = :a)
			""")
	int deleteBetween(Long a, Long b);
}
