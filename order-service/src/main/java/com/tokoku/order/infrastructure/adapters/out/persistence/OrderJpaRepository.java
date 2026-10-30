package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByIdAndCustomerId(Long id, String customerId);

	Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);

}
