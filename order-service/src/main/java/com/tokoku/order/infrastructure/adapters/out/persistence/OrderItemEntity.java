package com.tokoku.order.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
class OrderItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "order_id")
	private OrderEntity order;

	/** Product milik product-service: hanya ID, bukan relationship. */
	@Column(nullable = false)
	private long productId;

	@Column(nullable = false)
	private int quantity;

	protected OrderItemEntity() {
	}

	OrderItemEntity(long productId, int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	void setOrder(OrderEntity order) {
		this.order = order;
	}

	long getProductId() {
		return productId;
	}

	int getQuantity() {
		return quantity;
	}

}
