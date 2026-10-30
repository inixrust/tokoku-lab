package com.tokoku.product.application;

import java.math.BigDecimal;
import java.util.List;

import com.tokoku.product.domain.exception.DuplicateSkuException;
import com.tokoku.product.domain.exception.ProductNotFoundException;
import com.tokoku.product.domain.model.Product;
import com.tokoku.product.domain.port.out.ProductStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

	private final ProductStore products;

	public ProductService(ProductStore products) {
		this.products = products;
	}

	public Product get(long id) {
		return products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
	}

	public Page<Product> list(Pageable pageable) {
		return products.findAll(pageable);
	}

	public List<Product> search(String query) {
		return products.searchByName(query);
	}

	@Transactional
	public Product create(String sku, String name, BigDecimal price) {
		if (products.existsBySku(sku)) {
			throw new DuplicateSkuException(sku);
		}
		return products.save(Product.newProduct(sku, name, price));
	}

}
