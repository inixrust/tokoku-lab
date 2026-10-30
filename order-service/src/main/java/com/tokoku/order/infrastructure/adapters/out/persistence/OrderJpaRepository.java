package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.tokoku.order.domain.model.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByIdAndCustomerId(Long id, String customerId);

	// N+1 bila dipakai untuk daftar: items dimuat lazy per order saat dibaca (L6.1, L15.1 baseline).
	Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);

	Page<OrderEntity> findByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);

	// Tahap 1 (slide 128): paging atas ID saja, projection ringan (+ count query untuk Page).
	Page<IdOnly> findIdsByCustomerId(String customerId, Pageable pageable);

	Page<IdOnly> findIdsByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);

	// Tahap 2: satu query dengan items untuk ID tersebut (fetch plan eksplisit, tanpa Pageable).
	@EntityGraph(attributePaths = "items")
	List<OrderEntity> findByIdIn(Collection<Long> ids);

	record IdOnly(Long id) {
	}

}
