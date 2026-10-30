package com.tokoku.order.infrastructure.adapters.in.rest;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

/**
 * L19.1 langkah 2: slice web. Controller sudah dilindungi JWT sejak L11.1 (pola slide 99).
 *
 * <p>TODO:
 * <ul>
 * <li>Tanpa token: 401. Scope salah: 403.</li>
 * <li>Order bukan milik pemanggil: 404 application/problem+json.</li>
 * <li>Body invalid (lines kosong): 400 application/problem+json.</li>
 * </ul>
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

}
