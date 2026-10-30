package com.tokoku.order.application.service;

import java.util.List;

import com.tokoku.order.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.tokoku.order.domain.exception.InsufficientStockException;
import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;
import com.tokoku.order.domain.model.OrderStatus;
import com.tokoku.order.domain.port.out.OrderEvents;
import com.tokoku.order.domain.port.out.OrderStore;
import com.tokoku.order.domain.port.out.StockCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

/** Unit test tanpa Spring: cepat (milidetik), menguji aturan use case saja. */
@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

	@Mock
	OrderStore orders;

	@Mock
	OrderEvents events;

	@Mock
	StockCheck stock;

	@Mock
	TransactionTemplate transaction;

	PlaceOrderService service;

	@BeforeEach
	void setUp() {
		// TransactionTemplate tiruan: jalankan callback langsung.
		lenient().when(transaction.execute(any())).thenAnswer(
				invocation -> invocation.<TransactionCallback<Order>>getArgument(0).doInTransaction(null));
		lenient().when(orders.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 1001L));
		service = new PlaceOrderService(orders, events, stock, transaction);
	}

	@Test
	void availableStockSavesOrderAndPublishesEvent() {
		given(stock.check(anyLong(), anyInt())).willReturn(Availability.AVAILABLE);

		Order order = service.place(command(new OrderLine(1, 2)));

		assertThat(order.id()).isEqualTo(1001L);
		assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
		then(events).should().orderPlaced(order);
	}

	@Test
	void insufficientStockRejectsOrderWithoutSaving() {
		given(stock.check(5L, 3)).willReturn(Availability.INSUFFICIENT);

		assertThatExceptionOfType(InsufficientStockException.class)
			.isThrownBy(() -> service.place(command(new OrderLine(5, 3))))
			.satisfies(ex -> assertThat(ex.productId()).isEqualTo(5L));
		then(orders).shouldHaveNoInteractions();
		then(events).shouldHaveNoInteractions();
	}

	@Test
	void unknownStockStillPlacesOrderSoTheSagaDecides() {
		given(stock.check(anyLong(), anyInt())).willReturn(Availability.UNKNOWN);

		Order order = service.place(command(new OrderLine(1, 1)));

		assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
		then(events).should().orderPlaced(order);
	}

	private static PlaceOrderCommand command(OrderLine... lines) {
		return new PlaceOrderCommand("alice", List.of(lines));
	}

	private static Order withId(Order order, long id) {
		return new Order(id, order.customerId(), order.status(), order.createdAt(), order.lines());
	}

}
