package com.tokoku.product.domain.model;

/** Event domain (in-process): product berubah, salinan di cache harus dibuang. */
public record ProductUpdated(long productId) {
}
