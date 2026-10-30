package com.tokoku.order.infrastructure.adapters.in.rest;

import java.net.URI;

import com.tokoku.order.application.port.in.GetOrderUseCase;
import com.tokoku.order.application.port.in.PlaceOrderUseCase;
import com.tokoku.order.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;
import com.tokoku.order.infrastructure.adapters.in.rest.dto.OrderResponse;
import com.tokoku.order.infrastructure.adapters.in.rest.dto.PlaceOrderRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller tipis: validasi request, panggil use case, map DTO (slide 25). */
@RestController
@RequestMapping("/api/orders")
class OrderController {

	private final PlaceOrderUseCase placeOrder;

	private final GetOrderUseCase getOrder;

	OrderController(PlaceOrderUseCase placeOrder, GetOrderUseCase getOrder) {
		this.placeOrder = placeOrder;
		this.getOrder = getOrder;
	}

	@PostMapping
	ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
		var lines = request.lines().stream().map(line -> new OrderLine(line.productId(), line.quantity())).toList();
		Order order = placeOrder.place(new PlaceOrderCommand(request.customerId(), lines));
		return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(OrderResponse.from(order));
	}

	@GetMapping("/{id}")
	OrderResponse get(@PathVariable long id) {
		return OrderResponse.from(getOrder.get(id));
	}

	@GetMapping
	PagedModel<OrderResponse> list(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return new PagedModel<>(getOrder.list(pageable).map(OrderResponse::from));
	}

}
