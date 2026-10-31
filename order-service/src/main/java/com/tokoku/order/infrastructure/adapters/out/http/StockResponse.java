package com.tokoku.order.infrastructure.adapters.out.http;

/** Representasi response inventory-service; hanya field yang dibutuhkan (Tolerant Reader). */
public record StockResponse(long productId, int available) {
}
