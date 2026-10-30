package com.tokoku.order.application.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * L19.1 langkah 1: unit test tanpa Spring (harus jauh di bawah 1 detik).
 *
 * <p>TODO:
 * <ul>
 * <li>Mock OrderStore, OrderEvents, StockCheck, dan TransactionTemplate (jalankan callback-nya langsung).</li>
 * <li>Stok tersedia: order disimpan dan event OrderPlaced ditulis.</li>
 * <li>Stok kurang: InsufficientStockException, tidak ada yang disimpan.</li>
 * <li>Stok UNKNOWN (fallback breaker): order tetap disimpan PENDING.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

}
