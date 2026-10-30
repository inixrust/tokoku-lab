package com.tokoku.order.infrastructure.adapters.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.tokoku.order.domain.model.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Persistence model, bukan model domain (slide 43). */
@Entity
@Table(name = "orders")
class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
	@SequenceGenerator(name = "orders_seq", sequenceName = "orders_seq", allocationSize = 50)
	private Long id;

	@Version
	private long version;

	/** ID user dari identity provider: data service lain disimpan sebagai ID. */
	@Column(nullable = false, length = 64)
	private String customerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItemEntity> items = new ArrayList<>();

	protected OrderEntity() {
	}

	OrderEntity(String customerId, OrderStatus status, Instant createdAt) {
		this.customerId = customerId;
		this.status = status;
		this.createdAt = createdAt;
	}

	void addItem(OrderItemEntity item) {
		item.setOrder(this);
		items.add(item);
	}

	Long getId() {
		return id;
	}

	String getCustomerId() {
		return customerId;
	}

	OrderStatus getStatus() {
		return status;
	}

	void setStatus(OrderStatus status) {
		this.status = status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	List<OrderItemEntity> getItems() {
		return items;
	}

}
