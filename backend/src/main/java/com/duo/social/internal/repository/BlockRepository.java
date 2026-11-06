package com.duo.social.internal.repository;

import java.util.List;

import com.duo.social.internal.model.Block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BlockRepository extends JpaRepository<Block, Long> {

	boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	/** Bloqueio vale nos dois sentidos para quem enxerga quem. */
	@Query("""
			select count(b) > 0 from Block b
			where (b.blockerId = :a and b.blockedId = :b) or (b.blockerId = :b and b.blockedId = :a)
			""")
	boolean existsBetween(Long a, Long b);

	/** Todos os ids ligados a {@code me} por um bloqueio, de qualquer lado. */
	@Query("""
			select case when b.blockerId = :me then b.blockedId else b.blockerId end
			from Block b where b.blockerId = :me or b.blockedId = :me
			""")
	List<Long> relatedIds(Long me);

	List<Block> findTop200ByBlockerIdOrderByIdDesc(Long blockerId);

	@Modifying
	@Query("delete from Block b where b.blockerId = :blockerId and b.blockedId = :blockedId")
	int deleteByPair(Long blockerId, Long blockedId);
}
