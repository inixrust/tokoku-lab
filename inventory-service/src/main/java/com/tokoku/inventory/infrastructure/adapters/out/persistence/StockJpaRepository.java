package com.tokoku.inventory.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface StockJpaRepository extends JpaRepository<StockEntity, Long> {

}
