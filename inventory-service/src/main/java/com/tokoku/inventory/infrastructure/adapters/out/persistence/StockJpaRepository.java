package com.tokoku.inventory.infrastructure.adapters.out.persistence;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

interface StockJpaRepository extends JpaRepository<StockEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from StockEntity s where s.productId in :productIds order by s.productId")
	List<StockEntity> lockAll(Collection<Long> productIds);

}
