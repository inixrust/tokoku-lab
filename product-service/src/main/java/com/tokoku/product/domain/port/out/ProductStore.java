package com.tokoku.product.domain.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.tokoku.product.domain.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductStore {

	Optional<Product> findById(long id);

	Page<Product> findAll(Pageable pageable);

	List<Product> searchByName(String query);

	boolean existsBySku(String sku);

	Product save(Product product);

	/** Mengubah nama dan harga product yang sudah ada. */
	Product update(long id, String name, BigDecimal price);

}
