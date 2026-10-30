package com.tokoku.order.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

}
