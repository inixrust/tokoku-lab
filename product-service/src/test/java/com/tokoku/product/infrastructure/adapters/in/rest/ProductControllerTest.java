package com.tokoku.product.infrastructure.adapters.in.rest;

import java.math.BigDecimal;

import com.tokoku.product.application.ProductService;
import com.tokoku.product.domain.exception.DuplicateSkuException;
import com.tokoku.product.domain.exception.ProductNotFoundException;
import com.tokoku.product.domain.model.Product;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

	private static final Product KOPI = new Product(1L, "KOPI-ARB-250", "Kopi Arabika Gayo 250 g",
			new BigDecimal("85000"));

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ProductService products;

	@Test
	void versionOneAndTwoReturnDifferentShapes() throws Exception {
		given(products.get(1L)).willReturn(KOPI);

		mvc.perform(get("/api/products/1").header("API-Version", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.price").value(85000))
			.andExpect(jsonPath("$.sku").doesNotExist());

		mvc.perform(get("/api/products/1").header("API-Version", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sku").value("KOPI-ARB-250"))
			.andExpect(jsonPath("$.price.currency").value("IDR"));
	}

	@Test
	void missingOrUnsupportedVersionIsBadRequest() throws Exception {
		mvc.perform(get("/api/products/1"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

		mvc.perform(get("/api/products/1").header("API-Version", "3"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void unknownProductIsNotFoundProblem() throws Exception {
		given(products.get(999L)).willThrow(new ProductNotFoundException(999L));

		mvc.perform(get("/api/products/999").header("API-Version", "1"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
			.andExpect(jsonPath("$.productId").value(999));
	}

	@Test
	void duplicateSkuIsConflictProblem() throws Exception {
		given(products.create(any(), any(), any())).willThrow(new DuplicateSkuException("KOPI-ARB-250"));

		mvc.perform(post("/api/products").header("API-Version", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"sku": "KOPI-ARB-250", "name": "Kopi", "price": 85000}
						"""))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.errorCode").value("DUPLICATE_SKU"));
	}

	@Test
	void invalidBodyIsBadRequestProblem() throws Exception {
		mvc.perform(post("/api/products").header("API-Version", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"sku": "", "name": "Kopi", "price": -1}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.errors.length()").value(2));
	}

	@Test
	void tooShortQueryParameterIsBadRequestProblem() throws Exception {
		mvc.perform(get("/api/products/search").param("q", "ko").header("API-Version", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.errors[0].field").value("q"));
	}

	@Test
	void unexpectedErrorDoesNotLeakDetails() throws Exception {
		given(products.get(1L)).willThrow(new IllegalStateException("SELECT * FROM product failed"));

		mvc.perform(get("/api/products/1").header("API-Version", "1"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
			.andExpect(jsonPath("$.detail").value("Unexpected error. Contact support with the traceId."));
	}

}
