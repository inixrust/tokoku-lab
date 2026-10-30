package com.tokoku.product.application;

import java.math.BigDecimal;
import java.util.List;

import com.tokoku.product.domain.exception.DuplicateSkuException;
import com.tokoku.product.domain.exception.ProductNotFoundException;
import com.tokoku.product.domain.model.Product;
import com.tokoku.product.domain.model.ProductUpdated;
import com.tokoku.product.domain.port.out.ProductStore;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

	private final ProductStore products;

	private final ApplicationEventPublisher events;

	public ProductService(ProductStore products, ApplicationEventPublisher events) {
		this.products = products;
		this.events = events;
	}

	/** Cache key deterministik dari semua input (slide 144). Pantau hit ratio di metric cache.gets. */
	@Cacheable(cacheNames = "products", key = "#id")
	public Product get(long id) {
		return products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
	}

	public Page<Product> list(Pageable pageable) {
		return products.findAll(pageable);
	}

	public List<Product> search(String query) {
		return products.searchByName(query);
	}

	/** L17.1: perubahan harga menerbitkan ProductUpdated (dipakai untuk invalidation cache). */
	@Transactional
	public Product update(long id, String name, BigDecimal price) {
		Product updated = products.update(id, name, price);
		events.publishEvent(new ProductUpdated(id));
		return updated;
	}

	@Transactional
	public Product create(String sku, String name, BigDecimal price) {
		if (products.existsBySku(sku)) {
			throw new DuplicateSkuException(sku);
		}
		return products.save(Product.newProduct(sku, name, price));
	}

}
