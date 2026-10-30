package com.tokoku.product.infrastructure.adapters.in.rest;

import java.net.URI;
import java.util.List;

import com.tokoku.product.application.ProductService;
import com.tokoku.product.domain.model.Product;
import com.tokoku.product.infrastructure.adapters.in.rest.dto.CreateProductRequest;
import com.tokoku.product.infrastructure.adapters.in.rest.dto.ProductV1;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/products")
class ProductController {

	private final ProductService products;

	ProductController(ProductService products) {
		this.products = products;
	}

	@GetMapping
	PagedModel<ProductV1> list(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
		return new PagedModel<>(products.list(pageable).map(ProductV1::from));
	}

	@GetMapping("/{id}")
	ProductV1 get(@PathVariable long id) {
		return ProductV1.from(products.get(id));
	}

	@GetMapping("/search")
	List<ProductV1> search(@RequestParam @Size(min = 3) String q) {
		return products.search(q).stream().map(ProductV1::from).toList();
	}

	@PostMapping
	ResponseEntity<ProductV1> create(@Valid @RequestBody CreateProductRequest request) {
		Product created = products.create(request.sku(), request.name(), request.price());
		return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(ProductV1.from(created));
	}

}
