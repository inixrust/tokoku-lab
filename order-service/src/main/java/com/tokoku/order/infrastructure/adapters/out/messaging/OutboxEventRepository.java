package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

	/** SKIP LOCKED: setiap instance relay mengambil batch berbeda, tanpa saling menunggu (slide 60). */
	@Query(value = """
			select * from outbox where published_at is null
			order by id limit 100
			for update skip locked""", nativeQuery = true)
	List<OutboxEventEntity> lockNextBatch();

}
