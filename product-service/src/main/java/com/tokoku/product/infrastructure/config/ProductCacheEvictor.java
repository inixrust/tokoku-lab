package com.tokoku.product.infrastructure.config;

import com.tokoku.product.domain.model.ProductUpdated;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Invalidation berbasis event (slide 144). Setelah commit, supaya request lain tidak sempat mengisi cache
 * dengan nilai lama. Hanya membuang salinan di instance ini: instance lain tetap stale sampai TTL (Caffeine).
 */
@Component
class ProductCacheEvictor {

	@TransactionalEventListener
	@CacheEvict(cacheNames = "products", key = "#event.productId()")
	public void on(ProductUpdated event) {
	}

}
