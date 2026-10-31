package com.tokoku.order.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tokoku.order.application.port.in.PlaceOrderUseCase;
import com.tokoku.order.domain.exception.InsufficientStockException;
import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;
import com.tokoku.order.domain.port.out.OrderEvents;
import com.tokoku.order.domain.port.out.OrderStore;
import com.tokoku.order.domain.port.out.StockCheck;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
class PlaceOrderService implements PlaceOrderUseCase {

	private static final Map<Long, PlacedOrderTrace> RECENT_ORDERS = new ConcurrentHashMap<>();

	private final OrderStore orders;

	private final OrderEvents events;

	private final StockCheck stock;

	private final TransactionTemplate transaction;

	PlaceOrderService(OrderStore orders, OrderEvents events, StockCheck stock, TransactionTemplate transaction) {
		this.orders = orders;
		this.events = events;
		this.stock = stock;
		this.transaction = transaction;
	}

	@Override
	public Order place(PlaceOrderCommand command) {
		Order order = Order.place(command.customerId(), command.lines());
		// Panggilan remote di LUAR transaction: connection DB tidak tertahan selama menunggu inventory.
		for (OrderLine line : order.lines()) {
			if (checkStock(line) == Availability.INSUFFICIENT) {
				throw new InsufficientStockException(line.productId());
			}
		}
		// Order + event OrderPlaced di outbox dalam SATU local transaction (slide 59): commit sekali, atomik.
		Order placed = transaction.execute(status -> {
			Order saved = orders.save(order);
			events.orderPlaced(saved);
			return saved;
		});
		RECENT_ORDERS.put(placed.id(), new PlacedOrderTrace(placed, Instant.now(), Thread.currentThread().getStackTrace()));
		return placed;
	}

	private Availability checkStock(OrderLine line) {
		RuntimeException last = null;
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				return stock.check(line.productId(), line.quantity());
			}
			catch (RuntimeException ex) {
				last = ex;
			}
		}
		throw last;
	}

	/** Info diagnosis order yang baru dibuat. */
	record PlacedOrderTrace(Order order, Instant placedAt, StackTraceElement[] placedFrom) {
	}

}
