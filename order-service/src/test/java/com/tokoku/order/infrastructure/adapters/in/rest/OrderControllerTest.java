package com.tokoku.order.infrastructure.adapters.in.rest;

import com.tokoku.order.application.port.in.GetOrderUseCase;
import com.tokoku.order.application.port.in.PlaceOrderUseCase;
import com.tokoku.order.domain.exception.OrderNotFoundException;
import com.tokoku.order.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice web dengan SecurityConfig yang sebenarnya (slide 99). Tanpa @Import(SecurityConfig.class) dan token
 * {@code jwt()}, semua request di sini menjadi 401, bukan 404/400.
 */
@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

	private static final String ALICE = "7f2c1a4e-0b1d-4c55-9a6e-a11ce0000001";

	@Autowired
	MockMvc mvc;

	@MockitoBean
	GetOrderUseCase getOrder;

	@MockitoBean
	PlaceOrderUseCase placeOrder;

	/** Tidak memanggil Keycloak: token dibuat oleh jwt() post-processor. */
	@MockitoBean
	JwtDecoder jwtDecoder;

	@Test
	void missingTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/orders/1")).andExpect(status().isUnauthorized());
	}

	@Test
	void wrongScopeIsForbidden() throws Exception {
		mvc.perform(get("/api/orders/1").with(jwt().authorities(() -> "SCOPE_payments.write")))
			.andExpect(status().isForbidden());
	}

	@Test
	void orderOfAnotherCustomerIsNotFoundProblem() throws Exception {
		given(getOrder.get(1L, ALICE)).willThrow(new OrderNotFoundException(1L));

		mvc.perform(get("/api/orders/1")
				.with(jwt().jwt(token -> token.subject(ALICE)).authorities(() -> "SCOPE_orders.read")))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
	}

	@Test
	void invalidBodyIsBadRequestProblem() throws Exception {
		mvc.perform(post("/api/orders")
				.with(jwt().jwt(token -> token.subject(ALICE)).authorities(() -> "SCOPE_orders.write"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"lines": []}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.errors[0].field").value("lines"));
		then(placeOrder).should(never()).place(any());
	}

}
