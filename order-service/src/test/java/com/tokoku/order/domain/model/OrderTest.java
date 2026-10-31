package com.tokoku.order.domain.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OrderTest {

	@Test
	void newOrderIsPending() {
		Order order = Order.place("alice", List.of(new OrderLine(1, 2)));

		assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.id()).isNull();
	}

	@Test
	void orderNeedsAtLeastOneLine() {
		assertThatIllegalArgumentException().isThrownBy(() -> Order.place("alice", List.of()));
	}

	@Test
	void quantityMustBePositive() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OrderLine(1, 0));
	}

}
